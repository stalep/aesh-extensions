package org.jboss.aesh.extensions.cat;

import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.parser.Parser;
import org.jboss.aesh.terminal.Shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name = "cat", description = "concatenate files and print on the standard output")
public class Cat implements Command {

    @Option(shortName = 'A', name = "show-all", hasValue = false, description = "equivalent to -vET")
    private boolean showAll;

    @Option(shortName = 'b', name = "number-nonblank", hasValue = false, description = "number nonempty output lines, overrides -n")
    private boolean numberNonBlank;

    @Option(shortName = 'e', name = "show-ends", hasValue = false, description = "display $ at end of each line")
    private boolean showEnds;

    @Option(shortName = 'n', name = "number", hasValue = false, description = "number all output lines")
    private boolean number;

    @Option(shortName = 's', name = "squeeze-blank", hasValue = false, description = "suppress repeated empty output lines")
    private boolean squeezeBlank;

    @Option(shortName = 'T', name = "show-tabs", hasValue = false, description = "display TAB characters as ^I")
    private boolean showTabs;

    @Option(shortName = 'h', name = "help", hasValue = false, description = "display this help and exit")
    private boolean help;

    @Arguments
    private List<File> files;

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException {

        try {
            if(files != null && files.size() > 0) {
                for(File f : files)
                    displayFile(f, commandInvocation.getShell());

                return CommandResult.SUCCESS;
            }
            //read from stdin
            else {

                return CommandResult.SUCCESS;
            }
        }
        catch(FileNotFoundException fnfe) {
            commandInvocation.getShell().err().println("cat: "+fnfe.getMessage());
            return CommandResult.FAILURE;
        }

    }

    private void displayFile(File f, Shell shell) throws FileNotFoundException {
        BufferedReader br = new BufferedReader(new FileReader(f));

        String line = null;
        boolean prevBlank = false;
        boolean currentBlank = false;
        int counter = 1;
        try {
            line = br.readLine();
            while(line != null) {
                if(line.length() == 0) {
                    if(currentBlank && squeezeBlank)
                        prevBlank = true;
                    currentBlank = true;
                }
                else
                    prevBlank = currentBlank = false;

                if(numberNonBlank) {
                    if(!currentBlank) {
                        shell.out().print(Parser.padLeft(6, String.valueOf(counter)));
                        shell.out().print(' ');
                        counter++;
                    }
                }
                else if(number && !prevBlank) {
                    shell.out().print(Parser.padLeft(6, String.valueOf(counter)));
                    shell.out().print(' ');
                    counter++;
                }

                //todo
                if(showTabs) {
                    //if(line.contains())
                }
                else {
                    if(!prevBlank)
                        shell.out().print(line);
                }

                if(showEnds && !prevBlank)
                    shell.out().print('$');

                if(!prevBlank)
                    shell.out().print(Config.getLineSeparator());

                line = br.readLine();
            }
            shell.out().flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
