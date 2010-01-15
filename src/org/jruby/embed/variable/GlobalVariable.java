/**
 * **** BEGIN LICENSE BLOCK *****
 * Version: CPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Common Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2009 Yoko Harada <yokolet@gmail.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the CPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the CPL, the GPL or the LGPL.
 * **** END LICENSE BLOCK *****
 */
package org.jruby.embed.variable;

import org.jruby.embed.BiVariable;
import org.jruby.embed.internal.BiVariableMap;
import org.jruby.embed.BiVariable.Type;
import java.util.Set;
import org.jruby.Ruby;
import org.jruby.internal.runtime.GlobalVariables;
import org.jruby.runtime.builtin.IRubyObject;

/**
 *
 * @author Yoko Harada <yokolet@gmail.com>
 */
public class GlobalVariable extends AbstractVariable {
    public static BiVariable getInstance(Ruby runtime, String name, Object... javaObject) {
        String pattern = "\\$([a-zA-Z]|(_([a-zA-Z]|_|\\d)))([a-zA-Z]|_|\\d)*";
        if (name.matches(pattern)) {
            return new GlobalVariable(runtime, name, javaObject);
        }
        return null;
    }
    protected GlobalVariable(Ruby runtime, String name, Object... javaObject) {
        super(runtime, name, javaObject);
    }

    GlobalVariable(String name, IRubyObject irubyObject) {
        super(name, irubyObject);
    }

    public static void retrieve(Ruby runtime, IRubyObject receiver, BiVariableMap vars) {
        GlobalVariables gvars = runtime.getGlobalVariables();
        Set<String> names = gvars.getNames();
        for (String name : names) {
            if (isPredefined(name)) {
                continue;
            }
            BiVariable var;
            IRubyObject value = gvars.get(name);
            if (vars.containsKey((Object)name)) {
                var = vars.getVariable(name);
                var.setRubyObject(value);
            } else {
                var = new GlobalVariable(name, value);
                vars.update(name, var);
            }
        }
    }

    protected static boolean isPredefined(String name) {
        String[] patterns = {
            "\\$([\\u0021-\\u0040]|\\u005c|[\\u005e-\\u0060]|\\u007e)",
            "\\$-(\\d|[A-z])",
            "\\$(DEBUG|F|FILENAME|KCODE|LOAD_PATH|SAFE|VERBOSE|CLASSPATH|LOADED_FEATURES|PROGRAM_NAME)",
            "\\$(configure_args|deferr|defout|expect_verbose|stderr|stdin|stdout)"
        };
        for (String pattern : patterns) {
            if (name.matches(pattern)) {
                return true;
            }
        }
        return false;
    }

    public Type getType() {
        return Type.GlobalVariable;
    }

    public void inject(Ruby runtime, IRubyObject receiver) {
        runtime.getGlobalVariables().set(name, irubyObject);
    }

    public void remove(Ruby runtime) {
        setJavaObject(runtime, null);
        runtime.getGlobalVariables().set(name, irubyObject);
    }
}