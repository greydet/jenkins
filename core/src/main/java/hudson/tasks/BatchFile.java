/*
 * The MIT License
 * 
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., Kohsuke Kawaguchi
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.tasks;

import hudson.FilePath;
import hudson.Extension;
import hudson.model.AbstractProject;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import com.google.common.collect.ObjectArrays;

/**
 * Executes commands by using Windows batch file.
 *
 * @author Kohsuke Kawaguchi
 */
public class BatchFile extends CommandInterpreter {
    @DataBoundConstructor
    public BatchFile(String command) {
        super(command);
    }
    
    public String[] buildCommandLine(FilePath script) {
        return new String[] {"cmd","/c","call",script.getRemote()};
    }

    protected String[] buildCommandLine(FilePath script, FilePath ws) {
    	String[] cmdLine = buildCommandLine(script);
    	
    	if (ws.getRemote().startsWith("\\\\"))
    	{
    		// If workspace path is a UNC path, use pushd/popd to create a temporary drive and avoid cmd.exe to
    		// complain about unsupported UNC path and moving to C:\Windows as working directory
	    	String[] prependCmdLine = new String[] {"pushd",  "&&", "set", "WORKSPACE=%CD%", "&&"};
	    	String[] appendCmdLine = new String[] { "&&", "popd"};
	    	cmdLine = ObjectArrays.concat(prependCmdLine, cmdLine, String.class);
	    	cmdLine = ObjectArrays.concat(cmdLine, appendCmdLine, String.class);
    	}
    	
        return cmdLine;
    }

    protected String getContents() {
        return command+"\r\nexit %ERRORLEVEL%";
    }

    protected String getFileExtension() {
        return ".bat";
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @Override
        public String getHelpFile() {
            return "/help/project-config/batch.html";
        }

        public String getDisplayName() {
            return Messages.BatchFile_DisplayName();
        }

        @Override
        public Builder newInstance(StaplerRequest req, JSONObject data) {
            return new BatchFile(data.getString("command"));
        }

        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
