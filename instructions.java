public class instructions {

    public static class instruction {
        public String rs;
        public String rt;
        public String rd;
        public String type;

        public instruction() {
            this.rs = null;
            this.rt = null;
            this.rd = null;
            this.type = null;
        }
    }

    public static class And extends instruction {
        public String opcode;
        public String funct;
        public String shampt;

        public And(String rd, String rs, String rt) {
            super();
            this.opcode = "000000";
            this.rd = rd;
            this.rs = rs;
            this.rt = rt;
            this.funct = "100100";
            this.shampt = "00000";
            this.type = "r";
        }

        public void printObj() {
            System.out.print(this.opcode + " ");
            System.out.print(this.rs + " ");
            System.out.print(this.rt + " ");
            System.out.print(this.rd + " ");
            System.out.print(this.shampt + " ");
            System.out.print(this.funct);
        }
    }

    public static class Or extends instruction {
        public String opcode;
        public String funct;
        public String shampt;

        public Or(String rd, String rs, String rt) {
            super();
            this.opcode = "000000";
            this.rd = rd;
            this.rs = rs;
            this.rt = rt;
            this.funct = "100101";
            this.shampt = "00000";
            this.type = "r";
        }

        public void printObj() {
            System.out.print(this.opcode + " ");
            System.out.print(this.rs + " ");
            System.out.print(this.rt + " ");
            System.out.print(this.rd + " ");
            System.out.print(this.shampt + " ");
            System.out.print(this.funct);
        }
    }

    public static class Add extends instruction {
        public String opcode;
        public String funct;
        public String shampt;

        public Add(String rd, String rs, String rt) {
            super();
            this.opcode = "000000";
            this.rd = rd;
            this.rs = rs;
            this.rt = rt;
            this.funct = "100000";
            this.shampt = "00000";
        }

        public void printObj() {
            System.out.print(this.opcode + " ");
            System.out.print(this.rs + " ");
            System.out.print(this.rt + " ");
            System.out.print(this.rd + " ");
            System.out.print(this.shampt + " ");
            System.out.print(this.funct);
            this.type = "r";
        }
    }

    public static class Sll extends instruction {
        public String opcode;
        public String sa;
        public String funct;

        public Sll(String rd, String rt, String sa) {
            super();
            this.opcode = "000000";
            this.rd = rd;
            this.rt = rt;
            this.sa = sa;
            this.funct = "000000";
            this.type = "r";
        }

        public void printObj() {
            System.out.print(this.opcode + " ");
            System.out.print(this.rs + " ");
            System.out.print(this.rt + " ");
            System.out.print(this.rd + " ");
            System.out.print(this.sa + " ");
            System.out.print(this.funct);
        }
    }

    public static class Sub extends instruction {
        public String opcode;
        public String funct;
        public String shampt;

        public Sub(String rd, String rs, String rt) {
            super();
            this.opcode = "000000";
            this.rd = rd;
            this.rs = rs;
            this.rt = rt;
            this.funct = "100010";
            this.shampt = "00000";
            this.type = "r";
        }

        public void printObj() {
            System.out.print(this.opcode + " ");
            System.out.print(this.rs + " ");
            System.out.print(this.rt + " ");
            System.out.print(this.rd + " ");
            System.out.print(this.shampt + " ");
            System.out.print(this.funct);
        }
    }

    public static class Slt extends instruction {
        public String opcode;
        public String funct;
        public String shampt;

        public Slt(String rd, String rs, String rt) {
            super();
            this.opcode = "000000";
            this.rd = rd;
            this.rs = rs;
            this.rt = rt;
            this.funct = "101010";
            this.shampt = "00000";
            this.type = "r";
        }

        public void printObj() {
            System.out.print(this.opcode + " ");
            System.out.print(this.rs + " ");
            System.out.print(this.rt + " ");
            System.out.print(this.rd + " ");
            System.out.print(this.shampt + " ");
            System.out.print(this.funct);
        }
    }

    public static class Jr extends instruction {
        public String opcode;
        public String funct;

        public Jr(String rs) {
            super();
            this.opcode = "000000";
            this.rs = rs;
            this.funct = "001000";
            this.type = "r";
        }

        public void printObj() {
            System.out.print(this.opcode + " ");
            System.out.print(this.rs + " ");
            System.out.print(this.funct);
        }
    }

    public static class Addi extends instruction {
        public String opcode;
        public String rt;
        public String imm;

        public Addi(String rs, String rt, String imm) {
            super();
            this.opcode = "001000";
            this.rs = rs;
            this.rt = rt;
            this.imm = imm;
            this.type = "i";
        }

        public void printObj() {
            System.out.print(this.opcode + " ");
            System.out.print(this.rs + " ");
            System.out.print(this.rt + " ");
            System.out.print(this.imm);
        }
    }

    public static class Beq extends instruction {
        public String opcode;
        public String rt;
        public String offset;

        public Beq(String rs, String rt, String offset) {
            super();
            this.opcode = "000100";
            this.rs = rs;
            this.rt = rt;
            this.offset = offset;
            this.type = "i";
        }

        public void printObj() {
            System.out.print(this.opcode + " ");
            System.out.print(this.rs + " ");
            System.out.print(this.rt + " ");
            System.out.print(this.offset);
        }
    }

    public static class Bne extends instruction {
        public String opcode;
        public String rt;
        public String offset;

        public Bne(String rs, String rt, String offset) {
            super();
            this.opcode = "000101";
            this.rs = rs;
            this.rt = rt;
            this.offset = offset;
            this.type = "i";
        }

        public void printObj() {
            System.out.print(this.opcode + " ");
            System.out.print(this.rs + " ");
            System.out.print(this.rt + " ");
            System.out.print(this.offset);
        }
    }

    public static class Lw extends instruction {
        public String opcode;
        public String rt;
        public String offset;

        public Lw(String rt, String offset, String rs) {
            super();
            this.opcode = "100011";
            this.rs = rs;
            this.rt = rt;
            this.offset = offset;
            this.type = "i";
        }

        public void printObj() {
            System.out.print(this.opcode + " ");
            System.out.print(this.rs + " ");
            System.out.print(this.rt + " ");
            System.out.print(this.offset);
        }
    }
    
    public static class Sw extends instruction {
        public String opcode;
        public String rs;
        public String rt;
        public String offset;
        
        public Sw(String rt, String offset, String rs) {
            super();
            this.opcode = "101011";
            this.rs = rs;
            this.rt = rt;
            this.offset = offset;
            this.type = "i";
        }

        public void printObj() {
            System.out.print(this.opcode + " ");
            System.out.print(this.rs + " ");
            System.out.print(this.rt + " ");
            System.out.print(this.offset);
        }
    }

    public static class J extends instruction {
        public String opcode;
        public String target;
        
        public J(String target) {
            super();
            this.opcode = "000010";
            this.target = target;
            this.type = "j";
        }

        public void printObj() {
            System.out.print(this.opcode + " ");
            System.out.print(this.target);
        }
    }

    public static class Jal extends instruction {
        public String opcode;
        public String target;
        
        public Jal(String target) {
            super();
            this.opcode = "000011";
            this.target = target;
            this.type = "j";
        }
        public void printObj() {
            System.out.print(this.opcode + " ");
            System.out.print(this.target);
        }
    }
}