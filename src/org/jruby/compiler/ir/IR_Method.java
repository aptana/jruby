package org.jruby.compiler.ir;

import java.util.List;
import java.util.ArrayList;
import org.jruby.compiler.ir.instructions.IR_Instr;
import org.jruby.compiler.ir.instructions.ReceiveArgumentInstruction;
import org.jruby.compiler.ir.operands.Label;
import org.jruby.compiler.ir.operands.MetaObject;
import org.jruby.compiler.ir.operands.Operand;

public class IR_Method extends IR_ExecutionScope {
    public final String  _name;     // Ruby name 
    public final boolean _isInstanceMethod;

    public final Label _startLabel; // Label for the start of the method
    public final Label _endLabel;   // Label for the end of the method

    // SSS FIXME: Token can be final for a method -- implying that the token is only for this particular implementation of the method
    // But, if the mehod is modified, we create a new method object which in turn gets a new token.  What makes sense??  Intuitively,
    // it seems the first one ... but let us see ...
    private CodeVersion _token;   // Current code version token for this method -- can change during execution as methods get redefined!
    private List<Operand> _callArgs;

    public IR_Method(IR_Scope lexicalParent, Operand container, String name, boolean isInstanceMethod) {
        super(lexicalParent, container);
        _name = name;
        _isInstanceMethod = isInstanceMethod;
        _startLabel = getNewLabel("_METH_START");
        _endLabel   = getNewLabel("_METH_END");
        _callArgs = new ArrayList<Operand>();
        _token = CodeVersion.getVersionToken();
    }

    @Override
    public void addInstr(IR_Instr i) {
        // Accumulate call arguments
        if (i instanceof ReceiveArgumentInstruction)
            _callArgs.add(i._result);

        super.addInstr(i);
    }

    public Operand[] getCallArgs() { 
        return _callArgs.toArray(new Operand[_callArgs.size()]);
    }

    @Override
    public void setConstantValue(String constRef, Operand val) {
        if (isAClassRootMethod())
            ((MetaObject)_container)._scope.setConstantValue(constRef, val);
        else
            throw new org.jruby.compiler.NotCompilableException("Unexpected: Encountered set constant value in a method!");
    }

    public boolean isAClassRootMethod() { 
        return IR_Module.isAClassRootMethod(this);
    }

    // SSS FIXME: Incorect!
    public String getFullyQualifiedName() {
        IR_Module m = getDefiningModule();
        return (m == null) ? null : m._name + ":" + _name;
    }

    public IR_Module getDefiningModule() {
        return (_container instanceof MetaObject) ? (IR_Module)((MetaObject)_container)._scope : null;
    }

    public CodeVersion getCodeVersionToken() { 
        return _token; 
    }

    @Override
    public String toString() {
        return "Method: " + _name + super.toString();
    }
}
