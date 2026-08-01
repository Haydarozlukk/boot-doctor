package dev.aetlas.bootdoctor;

import dev.aetlas.bootdoctor.cli.BootDoctorCommand;
import picocli.CommandLine;

public final class BootDoctorApplication {

    private BootDoctorApplication() {}

    public static void main(String[] args) {
        int exitCode = new CommandLine(new BootDoctorCommand()).execute(args);
        System.exit(exitCode);
    }
}
