package consoleDisplay.console;

import consoleDisplay.validation.Validators;

import java.io.PrintStream;
import java.util.Scanner;

public final class ConsoleIO {
    private final Scanner in;
    private final PrintStream out;

    public ConsoleIO() {
        this.in = new Scanner(System.in);
        this.out = System.out;
    }

    public void println(String s) { System.out.println(s);}

    public void printf(String fmt, Object... args) {out.printf(fmt, args);}

    public String askLine(String prompt) {
        out.print(prompt);
        return in.nextLine();
    }

    public long[] askCsvLongs(String prompt) {
        while (true) {
            out.print(prompt);
            String line = in.nextLine();
            try {
                return Validators.parseLongsStrict(line);
            } catch (IllegalArgumentException e) {
                out.println("Invalid input: " + e.getMessage());
                out.println("For example: 7,-3,5 is a valid input. (TIP: leave empty for default input).");
            }
        }
    }

    public int askExpansionDegree(int max) {
        while (true) {
            out.print("Enter expansion degree (0-" + max + "). Press Enter for 0: ");
            String line = in.nextLine();
            try {
                int requested = (line == null || line.isBlank())
                        ? 0
                        : Integer.parseInt(line.trim());
                return Validators.validateExpansionDegreeOrThrow(requested, max);
            } catch (NumberFormatException nfe) {
                out.println("Invalid number: please enter an integer between 0 and " + max + ".");
            } catch (IllegalArgumentException iae) {
                out.println(iae.getMessage());
            }
        }
    }

    public void close() { in.close(); }

}


