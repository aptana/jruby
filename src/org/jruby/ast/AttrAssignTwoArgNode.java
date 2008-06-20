/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jruby.ast;

import org.jruby.Ruby;
import org.jruby.evaluator.ASTInterpreter;
import org.jruby.lexer.yacc.ISourcePosition;
import org.jruby.runtime.Block;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

/**
 *
 * @author enebo
 */
public class AttrAssignTwoArgNode extends AttrAssignNode {
    private Node arg1;
    private Node arg2;
    
    public AttrAssignTwoArgNode(ISourcePosition position, Node receiverNode, String name, ArrayNode argsNode) {
        super(position, receiverNode, name, argsNode);
        
        assert argsNode.size() == 2 : "argsNode.size() is 2";
        
        arg1 = argsNode.get(0);
        arg2 = argsNode.get(1);
    }

    @Override
    public IRubyObject interpret(Ruby runtime, ThreadContext context, IRubyObject self, Block aBlock) {
        IRubyObject receiver = getReceiverNode().interpret(runtime, context, self, aBlock);
        IRubyObject param1 = arg1.interpret(runtime, context, self, aBlock);
        IRubyObject param2 = arg2.interpret(runtime, context, self, aBlock);
        
        assert receiver.getMetaClass() != null : receiver.getClass().getName();
        
        // If reciever is self then we do the call the same way as vcall
        if (receiver == self) {
            variableCallAdapter.call(context, receiver, param1, param2);
        } else {
            normalCallAdapter.call(context, receiver, param1, param2);
        }

        return param2;
    }
    
        
    @Override
    public IRubyObject assign(Ruby runtime, ThreadContext context, IRubyObject self, IRubyObject value, Block aBlock, boolean checkArity) {
        IRubyObject receiver = getReceiverNode().interpret(runtime, context, self, aBlock);
        IRubyObject param1 = arg1.interpret(runtime, context, self, aBlock);
        IRubyObject param2 = arg2.interpret(runtime, context, self, aBlock);
        
        assert receiver.getMetaClass() != null : receiver.getClass().getName();
        
        // If reciever is self then we do the call the same way as vcall
        if (receiver == self) {
            variableCallAdapter.call(context, receiver, param1, param2, value);
        } else {
            normalCallAdapter.call(context, receiver, param1, param2, value);
        }
        
        return runtime.getNil();
    }
}