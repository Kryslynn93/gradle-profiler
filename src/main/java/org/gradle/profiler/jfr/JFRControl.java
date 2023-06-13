package org.gradle.profiler.jfr;

import com.google.common.collect.ImmutableMap;
import org.gradle.profiler.CommandExec;
import org.gradle.profiler.InstrumentingProfiler;
import org.gradle.profiler.flamegraph.DetailLevel;
import org.gradle.profiler.flamegraph.FlameGraphGenerator;
import org.gradle.profiler.flamegraph.FlameGraphSanitizer;
import org.gradle.profiler.flamegraph.Stacks;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JFRControl implements InstrumentingProfiler.SnapshotCapturingProfilerController {

    private final JcmdRunner jcmd;
    private final JFRArgs jfrArgs;
    private final File jfrFile;
    private final JfrToStacksConverter stacksConverter = new JfrToStacksConverter(ImmutableMap.of(
        DetailLevel.RAW, FlameGraphSanitizer.raw(),
        DetailLevel.SIMPLIFIED, FlameGraphSanitizer.simplified()
    ));
    private final FlameGraphGenerator flameGraphGenerator = new FlameGraphGenerator();
    private int counter;

    public JFRControl(JFRArgs args, File jfrFile) {
        this.jcmd = new JcmdRunner();
        this.jfrArgs = args;
        this.jfrFile = jfrFile;
    }

    @Override
    public void startRecording(String pid) throws IOException, InterruptedException {
        jcmd.run(pid, "JFR.start", "name=profile", "settings=" + jfrArgs.getJfrSettings());
    }

    @Override
    public void stopRecording(String pid) {
        File outputFile = jfrFile.isDirectory()
            ? new File(jfrFile, "jfr-" + (counter++) + ".jfr")
            : jfrFile;
        jcmd.run(pid, "JFR.stop", "name=profile", "filename=" + outputFile.getAbsolutePath());
    }

    @Override
    public void captureSnapshot(String pid) {
    }

    @Override
    public void stopSession() {
        String jfrFileName = jfrFile.getName();
        String outputBaseName = jfrFileName.substring(0, jfrFileName.length() - 4);
        List<Stacks> stackFiles = stacksConverter.generateStacks(jfrFile, outputBaseName);
        flameGraphGenerator.generateGraphs(jfrFile.getParentFile(), stackFiles);
        System.out.println("Wrote profiling data to " + jfrFile.getPath());
    }

    public String getName() {
        return "jfr";
    }

    private void run(String... commandLine) {
        new CommandExec().run(commandLine);
    }
}
