// TYLER BAXTER AND JACK HERBERGER

import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.ArrayList;

public class lab4 extends instructions {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage java Main <filename>");
            System.exit(1);
        }
        String filename = args[0];
        String scriptname;
        ArrayList<String> scriptLines = new ArrayList<>();
        if (args.length > 1) {
            scriptname = args[1];   
            Scanner scriptReader = new Scanner(new FileReader(scriptname)); 
            scriptLines = getScripts(scriptReader); 
        }
        Scanner reader = new Scanner(new FileReader(filename));
        Hashtable<String, String> reg_codes = buildRegisterTable();
        ArrayList<String> lines = getLines(reader);
        Hashtable<String, String> label_addresses = buildLabelTable(lines);
        ArrayList<Object> write = write(reg_codes, lines, label_addresses);
        
        if (scriptLines.size() == 0) {
            spim(write, reg_codes, label_addresses);
        } else {
            spimScript(write, reg_codes, label_addresses, scriptLines);
        }
    }

    public static void spim(ArrayList<Object> write, Hashtable<String, String> reg_codes, Hashtable<String, String> label_addresses) {
        int[] registers = new int[32];
        int[] data_memory = new int[8192];
        ArrayList<String> pipeline = new ArrayList<>();
        pipeline.add("empty");
        pipeline.add("empty");
        pipeline.add("empty");
        pipeline.add("empty");
        for (int i = 0; i < data_memory.length; i++) {
            data_memory[i] = 0;
        }
        Scanner scanner = new Scanner(System.in);

        final int[] pc = {0, 0}; // 0 is regular pc, 1 is for where to jump to with branch
        final int[] cycles = {0}; 
        final int[] lw_flag = {0, 0}; // 0 is if there was a lw last iteration, 1 is is squash needs to be printed due to conditions
        final int[] b_flag = {0, 0}; // 0 is set to 3 for 3 rounds before squash, 1 is for if last three need to turn to squash
        final int[] j_flag = {0}; // 0 is for the flag,
        final int[] instr_count = {0}; // Seperate variable to count instructions
        final int[] print_flag = {0}; // Print flag

        Runnable runnable = new Runnable() {
            public void run() {

                cycles[0] += 1;  
                Object curr = write.get(pc[0]);  
                if (print_flag[0] == 0){
                    System.out.println();
                }  

                if (b_flag[0] > 1) {
                    cycles[0] += 1;
                    b_flag[0] -= 1;
                    int pc_offset = -(b_flag[0]) + 3;
                    pipeline.add(0, curr.getClass().toString().substring(19).toLowerCase());
                    if (print_flag[0] == 0){
                        System.out.format("%2s %10s %10s %10s %10s\n", "pc", "if/id", "id/exe", "exe/mem", "mem/wb");
                        System.out.format("%2d %10s %10s %10s %10s\n\n", pc[0] + pc_offset, pipeline.get(0), pipeline.get(1), pipeline.get(2), pipeline.get(3));
                    }
                    pipeline.remove(pipeline.size()-1); 
                    return;
                } 
                else if (b_flag[0] == 1){
                    cycles[0] += 1;
                    pipeline.remove(0);
                    pipeline.remove(0);
                    pipeline.add(0, "squash");
                    pipeline.add(0, "squash");
                    pipeline.add(0, "squash");
                    if (print_flag[0] == 0){
                        System.out.format("%2s %10s %10s %10s %10s\n", "pc", "if/id", "id/exe", "exe/mem", "mem/wb");
                        System.out.format("%2d %10s %10s %10s %10s\n\n", pc[0] + 3, pipeline.get(0), pipeline.get(1), pipeline.get(2), pipeline.get(3));
                    }        
                    pc[0] = pc[1] + 1;
                    curr = write.get(pc[0]);
                    b_flag[0] -= 1;
                    lw_flag[0] = 0;
                    lw_flag[1] = 0;
                    j_flag[0] = 0;
                    return;
                }
                else if (lw_flag[1] > 0) {
                    // cycles[0] -= 1;
                    lw_flag[1] -= 1;
                    pipeline.add(1, "stall");
                    if (print_flag[0] == 0) {
                        System.out.format("%2s %10s %10s %10s %10s\n", "pc", "if/id", "id/exe", "exe/mem", "mem/wb");
                        System.out.format("%2d %10s %10s %10s %10s\n\n", pc[0], pipeline.get(0), pipeline.get(1), pipeline.get(2), pipeline.get(3));
                    }
                    pipeline.remove(pipeline.size()-1); 
                    return;
                } else if (j_flag[0] > 0) {
                    cycles[0] += 1;
                    pipeline.add(0, "squash");
                    if (print_flag[0] == 0) {
                        System.out.format("%2s %10s %10s %10s %10s\n", "pc", "if/id", "id/exe", "exe/mem", "mem/wb");
                        System.out.format("%2d %10s %10s %10s %10s\n\n", pc[0], pipeline.get(0), pipeline.get(1), pipeline.get(2), pipeline.get(3));
                    }
                    pipeline.remove(pipeline.size()-1); 
                    j_flag[0] -= 1;
                    return;
                } else {
                    instr_count[0] += 1;
                    pipeline.add(0, curr.getClass().toString().substring(19).toLowerCase());
                    if (print_flag[0] == 0){
                        System.out.format("%2s %10s %10s %10s %10s\n", "pc", "if/id", "id/exe", "exe/mem", "mem/wb");
                        System.out.format("%2d %10s %10s %10s %10s\n\n", pc[0] + 1, pipeline.get(0), pipeline.get(1), pipeline.get(2), pipeline.get(3));
                    }
                    pipeline.remove(pipeline.size()-1); 
                }

                if (lw_flag[0] > 0) {
                    lw_flag[0] -= 1;
                    // cycles[0] += 1;
                    instruction current = (instruction) curr;
                    Lw obj = (Lw) write.get(pc[0]-1);
                    int rs = Integer.parseInt(obj.rs, 2);
                    int rt = Integer.parseInt(obj.rt, 2);

                    if (current.type == 'r') {
                        if (current.rs != null) {
                            int next_rs = Integer.parseInt(current.rs, 2);
                            if (rs == next_rs) {
                                lw_flag[1] += 1; 
                            }
                        }
                        if (current.rt != null) {
                            int next_rt = Integer.parseInt(current.rt, 2);
                            if (rs == next_rt) {
                                lw_flag[1] += 1; 
                            }
                        }
                    }
                    if (current.type == 'i') {
                        if (current.rs != null) {
                            int next_rs = Integer.parseInt(current.rs, 2);
                            if (rs == next_rs) {
                                lw_flag[1] += 1; 
                            }
                        }
                    }
                    if (curr.getClass().equals(instructions.Jal.class)){
                        if (rs == 0b11111) {
                            cycles[0] += 1;

                            lw_flag[0] += 1; 
                        }
                    }
                    int offset = Integer.parseInt(obj.offset, 2);
                    registers[rt] = data_memory[registers[rs]+offset];
                } 

                if (curr.getClass().equals(instructions.And.class)){
                    And obj = (And) curr;
                    int rs = Integer.parseInt(obj.rs, 2);
                    int rt = Integer.parseInt(obj.rt, 2);
                    int rd = Integer.parseInt(obj.rd, 2);
                    registers[rd] = registers[rs] & registers[rt];
                }
                else if (curr.getClass().equals(instructions.Or.class)){
                    Or obj = (Or) curr;
                    int rs = Integer.parseInt(obj.rs, 2);
                    int rt = Integer.parseInt(obj.rt, 2);
                    int rd = Integer.parseInt(obj.rd, 2);
                    registers[rd] = (registers[rs] | registers[rt]);
                }
                else if (curr.getClass().equals(instructions.Add.class)){
                    Add obj = (Add) curr;
                    int rs = Integer.parseInt(obj.rs, 2);
                    int rt = Integer.parseInt(obj.rt, 2);
                    int rd = Integer.parseInt(obj.rd, 2);
                    registers[rd] = registers[rs] + registers[rt];
                }
                else if (curr.getClass().equals(instructions.Sll.class)){
                    Sll obj = (Sll) curr;
                    int rd = Integer.parseInt(obj.rd, 2);
                    int rt = Integer.parseInt(obj.rt, 2);
                    int sa = Integer.parseInt(obj.sa, 2);
                    registers[rd] = registers[rt] << sa;
                }
                else if (curr.getClass().equals(instructions.Sub.class)){
                    Sub obj = (Sub) curr;
                    int rs = Integer.parseInt(obj.rs, 2);
                    int rt = Integer.parseInt(obj.rt, 2);
                    int rd = Integer.parseInt(obj.rd, 2);
                    registers[rd] = registers[rs] - registers[rt];
                }
                else if (curr.getClass().equals(instructions.Slt.class)){
                    Slt obj = (Slt) curr;
                    int rs = Integer.parseInt(obj.rs, 2);
                    int rt = Integer.parseInt(obj.rt, 2);
                    int rd = Integer.parseInt(obj.rd, 2);
                    int set = 0;
                    if (registers[rs] < registers[rt]) { 
                        set = 1;
                    }
                    registers[rd] = set;
                }
                else if (curr.getClass().equals(instructions.Jr.class)){
                    Jr obj = (Jr) curr;
                    int rs = Integer.parseInt(obj.rs, 2);
                    pc[0] = registers[rs];
                    j_flag[0] += 1;
                }
                else if (curr.getClass().equals(instructions.Addi.class)){
                    Addi obj = (Addi) curr;
                    int rs = Integer.parseInt(obj.rs, 2);
                    int rt = Integer.parseInt(obj.rt, 2);
                    int imm = Integer.parseInt(obj.imm, 2);
                    if (obj.imm.charAt(0) == '0') {
                        registers[rt] = registers[rs] + imm;
                    } else {
                        byte b_offset = (byte)((int) imm);
                        registers[rt] = registers[rs] + b_offset;

                    }
                    
                }
                else if (curr.getClass().equals(instructions.Beq.class)){
                    Beq obj = (Beq) curr;
                    int rs = Integer.parseInt(obj.rs, 2);
                    int rt = Integer.parseInt(obj.rt, 2);
                    int offset = Integer.parseInt(obj.offset, 2);
                    byte b_offset = (byte)((int)offset);
                    if (registers[rs] == registers[rt]){
                        b_flag[0] = 3;
                        pc[1] = pc[0] + b_offset; // pc keeps going down always so we can always get next three inst
                    }
                }
                else if (curr.getClass().equals(instructions.Bne.class)){
                    Bne obj = (Bne) curr;
                    int rs = Integer.parseInt(obj.rs, 2);
                    int rt = Integer.parseInt(obj.rt, 2);
                    int offset = Integer.parseInt(obj.offset, 2);
                    byte b_offset = (byte)((int)offset);
                    if (registers[rs] != registers[rt]){
                        b_flag[0] = 3;
                        pc[1] = pc[0] + b_offset;
                    }
                }
                else if (curr.getClass().equals(instructions.Lw.class)){
                    Lw obj = (Lw) curr;
                    int rs = Integer.parseInt(obj.rs, 2);
                    int rt = Integer.parseInt(obj.rt, 2);
                    int offset = Integer.parseInt(obj.offset, 2);
                    registers[rt] = data_memory[registers[rs]+offset];
                    lw_flag[0] += 1;
                }
                else if (curr.getClass().equals(instructions.Sw.class)){
                    Sw obj = (Sw) curr;
                    int rs = Integer.parseInt(obj.rs, 2);
                    int rt = Integer.parseInt(obj.rt, 2);
                    int offset = Integer.parseInt(obj.offset, 2);
                    data_memory[registers[rs]+offset] = registers[rt];
                }
                else if (curr.getClass().equals(instructions.J.class)){
                    J obj = (J)curr;
                    pc[0] = Integer.parseInt(obj.target, 2) - 1;
                    j_flag[0] += 1;
                }
                else if (curr.getClass().equals(instructions.Jal.class)){
                    Jal obj = (Jal)curr;
                    registers[31] = pc[0];
                    pc[0] = Integer.parseInt(obj.target, 2) - 1;
                    j_flag[0] += 1;
                }
                pc[0]++;
            }
        };


        while (true) {
            System.out.print("mips> ");

            String input = scanner.nextLine();
            if (input.length() == 0) {
                System.out.print("Please enter a command");
            }
            if (input.charAt(0) == 'q') {
                break;
            }

            else if (input.charAt(0) == 'm') {
                System.out.println(" ");
                int space1 = input.indexOf(' ');
                int space2 = input.indexOf(' ', space1+1);
                int loc1 = Integer.parseInt(input.substring(space1+1, space2));
                int loc2 = Integer.parseInt(input.substring(space2+1, input.length()));
                for (int i = loc1; i <= loc2; i++) {
                    System.out.println("[" + i + "]" + " = " + data_memory[i]);
                }
                System.out.println(" ");
            }

            else if (input.charAt(0) == 's') {
                if (input.length() > 1) {
                    int space = input.indexOf(' ');
                    int loc = Integer.parseInt(input.substring(space+1, input.length()));
                    //System.out.println("    " + loc + " instruction(s) executed");
                    for (int i = 0; i < loc; i ++) {
                        if (pc[0] < write.size()){
                            runnable.run();
                        }
                    }
                }
                else {
                    if (pc[0] < write.size()){  
                        runnable.run();
                    }
                }
            }

            else if (input.charAt(0) == 'r') {
                print_flag[0] = 1;
                while (pc[0] < write.size()) {
                    runnable.run();
                }
                System.out.println("\nProgram complete");
                System.out.format("CPI = %.3f     Cycles = %d      Instructions = %d\n", (double) cycles[0] / instr_count[0], cycles[0], instr_count[0]);
                break;
            }
            
            else if (input.charAt(0) == 'c'){
                System.out.println("Simulator reset\n");
                for (int i = 0; i < 32; i++) {
                    registers[i] = 0;
                }
                for (int i = 0; i < 8192; i++) {
                    data_memory[i] = 0;
                }
                pc[0] = 0;
            }
            
            else if (input.charAt(0) == 'h') {
                System.out.print("\nh = show help\nd = dump register state\ns = single step through the program (i.e. execute 1 instruction and stop)\ns num = step through num instructions of the program\nr = run until the program ends\nm num1 num2 = display data memory from location num1 to num2\nc = clear all registers, memory, and the program counter to 0\nq = exit the program\n\n");
            }
            
            else if (input.charAt(0) == 'd') {
                int ra = 0;
                if (registers[31] != 0){
                    ra = registers[31] + 1;
                }
                System.out.println("\npc = " + pc[0]);
                System.out.format("" + 
                "$0 = %d          $v0 = %d         $v1 = %d         $a0 = %d \n" +
                "$a1 = %d         $a2 = %d         $a3 = %d         $t0 = %d\n" +
                "$t1 = %d         $t2 = %d         $t3 = %d         $t4 = %d\n" +
                "$t5 = %d         $t6 = %d         $t7 = %d         $s0 = %d\n" +
                "$s1 = %d         $s2 = %d         $s3 = %d         $s4 = %d\n" +
                "$s5 = %d         $s6 = %d         $s7 = %d         $t8 = %d\n" +
                "$t9 = %d         $sp = %d         $ra = %d\n\n", 
                registers[0], registers[2],registers[3], registers[4], 
                registers[5], registers[6], registers[7], registers[8], 
                registers[9], registers[10], registers[11], registers[12], 
                registers[13], registers[14], registers[15], registers[16], 
                registers[17], registers[18], registers[19], registers[20], 
                registers[21], registers[22], registers[23], registers[24], 
                registers[25], registers[29], ra);
            }
        }
        scanner.close();
    }

    public static void spimScript(ArrayList<Object> write, Hashtable<String, String> reg_codes, Hashtable<String, String> label_addresses,ArrayList<String> scriptLines) {
        int[] registers = new int[32];
        int[] data_memory = new int[8192];
        ArrayList<String> pipeline = new ArrayList<>();
        pipeline.add("empty");
        pipeline.add("empty");
        pipeline.add("empty");
        pipeline.add("empty");
        for (int i = 0; i < data_memory.length; i++) {
            data_memory[i] = 0;
        }
        Scanner scanner = new Scanner(System.in);

        final int[] pc = {0, 0}; // 0 is regular pc, 1 is for where to jump to with branch
        final int[] cycles = {0}; 
        final int[] lw_flag = {0, 0}; // 0 is if there was a lw last iteration, 1 is is squash needs to be printed due to conditions
        final int[] b_flag = {0, 0}; // 0 is set to 3 for 3 rounds before squash, 1 is for if last three need to turn to squash
        final int[] j_flag = {0}; // 0 is for the flag,
        final int[] instr_count = {0}; // Seperate variable to count instructions

        final int[] print_flag = {0}; // Print flag

        Runnable runnable = new Runnable() {
            public void run() {

                cycles[0] += 1;  
                Object curr = write.get(pc[0]);  
                if (print_flag[0] == 0){
                    System.out.println();
                }  

                if (b_flag[0] > 1) {
                    cycles[0] += 1;
                    b_flag[0] -= 1;
                    int pc_offset = -(b_flag[0]) + 3;
                    pipeline.add(0, curr.getClass().toString().substring(19).toLowerCase());
                    if (print_flag[0] == 0){
                        System.out.format("%2s %10s %10s %10s %10s\n", "pc", "if/id", "id/exe", "exe/mem", "mem/wb");
                        System.out.format("%2d %10s %10s %10s %10s\n\n", pc[0] + pc_offset, pipeline.get(0), pipeline.get(1), pipeline.get(2), pipeline.get(3));
                    }
                    pipeline.remove(pipeline.size()-1); 
                    return;
                } 
                else if (b_flag[0] == 1){
                    cycles[0] += 1;
                    pipeline.remove(0);
                    pipeline.remove(0);
                    pipeline.add(0, "squash");
                    pipeline.add(0, "squash");
                    pipeline.add(0, "squash");
                    if (print_flag[0] == 0){
                        System.out.format("%2s %10s %10s %10s %10s\n", "pc", "if/id", "id/exe", "exe/mem", "mem/wb");
                        System.out.format("%2d %10s %10s %10s %10s\n\n", pc[0] + 3, pipeline.get(0), pipeline.get(1), pipeline.get(2), pipeline.get(3));
                    }        
                    pc[0] = pc[1] + 1;
                    curr = write.get(pc[0]);
                    b_flag[0] -= 1;
                    lw_flag[0] = 0;
                    lw_flag[1] = 0;
                    j_flag[0] = 0;
                    return;
                }
                else if (lw_flag[1] > 0) {
                    // cycles[0] -= 1;
                    lw_flag[1] -= 1;
                    pipeline.add(1, "stall");
                    if (print_flag[0] == 0) {
                        System.out.format("%2s %10s %10s %10s %10s\n", "pc", "if/id", "id/exe", "exe/mem", "mem/wb");
                        System.out.format("%2d %10s %10s %10s %10s\n\n", pc[0], pipeline.get(0), pipeline.get(1), pipeline.get(2), pipeline.get(3));
                    }
                    pipeline.remove(pipeline.size()-1); 
                    return;
                } else if (j_flag[0] > 0) {
                    cycles[0] += 1;
                    pipeline.add(0, "squash");
                    if (print_flag[0] == 0) {
                        System.out.format("%2s %10s %10s %10s %10s\n", "pc", "if/id", "id/exe", "exe/mem", "mem/wb");
                        System.out.format("%2d %10s %10s %10s %10s\n\n", pc[0], pipeline.get(0), pipeline.get(1), pipeline.get(2), pipeline.get(3));
                    }
                    pipeline.remove(pipeline.size()-1); 
                    j_flag[0] -= 1;
                    return;
                } else {
                    instr_count[0] += 1;
                    pipeline.add(0, curr.getClass().toString().substring(19).toLowerCase());
                    if (print_flag[0] == 0){
                        System.out.format("%2s %10s %10s %10s %10s\n", "pc", "if/id", "id/exe", "exe/mem", "mem/wb");
                        System.out.format("%2d %10s %10s %10s %10s\n\n", pc[0] + 1, pipeline.get(0), pipeline.get(1), pipeline.get(2), pipeline.get(3));
                    }
                    pipeline.remove(pipeline.size()-1); 
                }

                if (lw_flag[0] > 0) {
                    lw_flag[0] -= 1;
                    // cycles[0] += 1;
                    instruction current = (instruction) curr;
                    Lw obj = (Lw) write.get(pc[0]-1);
                    int rs = Integer.parseInt(obj.rs, 2);
                    int rt = Integer.parseInt(obj.rt, 2);

                    if (current.type == 'r') {
                        if (current.rs != null) {
                            int next_rs = Integer.parseInt(current.rs, 2);
                            if (rs == next_rs) {
                                lw_flag[1] += 1; 
                            }
                        }
                        if (current.rt != null) {
                            int next_rt = Integer.parseInt(current.rt, 2);
                            if (rs == next_rt) {
                                lw_flag[1] += 1; 
                            }
                        }
                    }
                    if (current.type == 'i') {
                        if (current.rs != null) {
                            int next_rs = Integer.parseInt(current.rs, 2);
                            if (rs == next_rs) {
                                lw_flag[1] += 1; 
                            }
                        }
                    }
                    if (curr.getClass().equals(instructions.Jal.class)){
                        if (rs == 0b11111) {
                            cycles[0] += 1;

                            lw_flag[0] += 1; 
                        }
                    }
                    int offset = Integer.parseInt(obj.offset, 2);
                    registers[rt] = data_memory[registers[rs]+offset];
                } 

                if (curr.getClass().equals(instructions.And.class)){
                    And obj = (And) curr;
                    int rs = Integer.parseInt(obj.rs, 2);
                    int rt = Integer.parseInt(obj.rt, 2);
                    int rd = Integer.parseInt(obj.rd, 2);
                    registers[rd] = registers[rs] & registers[rt];
                }
                else if (curr.getClass().equals(instructions.Or.class)){
                    Or obj = (Or) curr;
                    int rs = Integer.parseInt(obj.rs, 2);
                    int rt = Integer.parseInt(obj.rt, 2);
                    int rd = Integer.parseInt(obj.rd, 2);
                    registers[rd] = (registers[rs] | registers[rt]);
                }
                else if (curr.getClass().equals(instructions.Add.class)){
                    Add obj = (Add) curr;
                    int rs = Integer.parseInt(obj.rs, 2);
                    int rt = Integer.parseInt(obj.rt, 2);
                    int rd = Integer.parseInt(obj.rd, 2);
                    registers[rd] = registers[rs] + registers[rt];
                }
                else if (curr.getClass().equals(instructions.Sll.class)){
                    Sll obj = (Sll) curr;
                    int rd = Integer.parseInt(obj.rd, 2);
                    int rt = Integer.parseInt(obj.rt, 2);
                    int sa = Integer.parseInt(obj.sa, 2);
                    registers[rd] = registers[rt] << sa;
                }
                else if (curr.getClass().equals(instructions.Sub.class)){
                    Sub obj = (Sub) curr;
                    int rs = Integer.parseInt(obj.rs, 2);
                    int rt = Integer.parseInt(obj.rt, 2);
                    int rd = Integer.parseInt(obj.rd, 2);
                    registers[rd] = registers[rs] - registers[rt];
                }
                else if (curr.getClass().equals(instructions.Slt.class)){
                    Slt obj = (Slt) curr;
                    int rs = Integer.parseInt(obj.rs, 2);
                    int rt = Integer.parseInt(obj.rt, 2);
                    int rd = Integer.parseInt(obj.rd, 2);
                    int set = 0;
                    if (registers[rs] < registers[rt]) { 
                        set = 1;
                    }
                    registers[rd] = set;
                }
                else if (curr.getClass().equals(instructions.Jr.class)){
                    Jr obj = (Jr) curr;
                    int rs = Integer.parseInt(obj.rs, 2);
                    pc[0] = registers[rs];
                    j_flag[0] += 1;
                }
                else if (curr.getClass().equals(instructions.Addi.class)){
                    Addi obj = (Addi) curr;
                    int rs = Integer.parseInt(obj.rs, 2);
                    int rt = Integer.parseInt(obj.rt, 2);
                    int imm = Integer.parseInt(obj.imm, 2);
                    if (obj.imm.charAt(0) == '0') {
                        registers[rt] = registers[rs] + imm;
                    } else {
                        byte b_offset = (byte)((int) imm);
                        registers[rt] = registers[rs] + b_offset;

                    }
                    
                }
                else if (curr.getClass().equals(instructions.Beq.class)){
                    Beq obj = (Beq) curr;
                    int rs = Integer.parseInt(obj.rs, 2);
                    int rt = Integer.parseInt(obj.rt, 2);
                    int offset = Integer.parseInt(obj.offset, 2);
                    byte b_offset = (byte)((int)offset);
                    if (registers[rs] == registers[rt]){
                        b_flag[0] = 3;
                        pc[1] = pc[0] + b_offset; // pc keeps going down always so we can always get next three inst
                    }
                }
                else if (curr.getClass().equals(instructions.Bne.class)){
                    Bne obj = (Bne) curr;
                    int rs = Integer.parseInt(obj.rs, 2);
                    int rt = Integer.parseInt(obj.rt, 2);
                    int offset = Integer.parseInt(obj.offset, 2);
                    byte b_offset = (byte)((int)offset);
                    if (registers[rs] != registers[rt]){
                        b_flag[0] = 3;
                        pc[1] = pc[0] + b_offset;
                    }
                }
                else if (curr.getClass().equals(instructions.Lw.class)){
                    Lw obj = (Lw) curr;
                    int rs = Integer.parseInt(obj.rs, 2);
                    int rt = Integer.parseInt(obj.rt, 2);
                    int offset = Integer.parseInt(obj.offset, 2);
                    registers[rt] = data_memory[registers[rs]+offset];
                    lw_flag[0] += 1;
                }
                else if (curr.getClass().equals(instructions.Sw.class)){
                    Sw obj = (Sw) curr;
                    int rs = Integer.parseInt(obj.rs, 2);
                    int rt = Integer.parseInt(obj.rt, 2);
                    int offset = Integer.parseInt(obj.offset, 2);
                    data_memory[registers[rs]+offset] = registers[rt];
                }
                else if (curr.getClass().equals(instructions.J.class)){
                    J obj = (J)curr;
                    pc[0] = Integer.parseInt(obj.target, 2) - 1;
                    j_flag[0] += 1;
                }
                else if (curr.getClass().equals(instructions.Jal.class)){
                    Jal obj = (Jal)curr;
                    registers[31] = pc[0];
                    pc[0] = Integer.parseInt(obj.target, 2) - 1;
                    j_flag[0] += 1;
                }
                pc[0]++;
            }
        };


        int index = -1;

        while (true) {
            System.out.print("mips> ");
            index++;
            String input;
            if (index < scriptLines.size()) {
                input = scriptLines.get(index);
            }
            else {
                break;
            }
            System.out.println(input);

            if (input.length() == 0) {
                System.out.print("Please enter a command");
            }
            if (input.charAt(0) == 'q') {
                break;
            }

            else if (input.charAt(0) == 'm') {
                System.out.println(" ");
                int space1 = input.indexOf(' ');
                int space2 = input.indexOf(' ', space1+1);
                int loc1 = Integer.parseInt(input.substring(space1+1, space2));
                int loc2 = Integer.parseInt(input.substring(space2+1, input.length()));
                for (int i = loc1; i <= loc2; i++) {
                    System.out.println("[" + i + "]" + " = " + data_memory[i]);
                }
                System.out.println(" ");
            }

            else if (input.charAt(0) == 's') {
                if (input.length() > 1) {
                    int space = input.indexOf(' ');
                    int loc = Integer.parseInt(input.substring(space+1, input.length()));
                    //System.out.println("    " + loc + " instruction(s) executed");
                    for (int i = 0; i < loc; i ++) {
                        if (pc[0] < write.size()){
                            runnable.run();
                        }
                    }
                }
                else {
                    if (pc[0] < write.size()){  
                        runnable.run();
                    }
                }
            }

            else if (input.charAt(0) == 'r') {
                print_flag[0] = 1;
                while (pc[0] < write.size()) {
                    runnable.run();
                }
                System.out.println("\nProgram complete");
                System.out.format("CPI = %.3f     Cycles = %d      Instructions = %d\n", (double) cycles[0] / instr_count[0], cycles[0], instr_count[0]);
                break;
            }
            
            else if (input.charAt(0) == 'c'){
                System.out.println("Simulator reset\n");
                for (int i = 0; i < 32; i++) {
                    registers[i] = 0;
                }
                for (int i = 0; i < 8192; i++) {
                    data_memory[i] = 0;
                }
                pc[0] = 0;
            }
            
            else if (input.charAt(0) == 'h') {
                System.out.print("\nh = show help\nd = dump register state\ns = single step through the program (i.e. execute 1 instruction and stop)\ns num = step through num instructions of the program\nr = run until the program ends\nm num1 num2 = display data memory from location num1 to num2\nc = clear all registers, memory, and the program counter to 0\nq = exit the program\n\n");
            }
            
    
            else if (input.charAt(0) == 'd') {
                int ra = 0;
                if (registers[31] != 0){
                    ra = registers[31] + 1;
                }
                System.out.println("\npc = " + pc[0]);
                System.out.format("" + 
                "$0 = %d          $v0 = %d         $v1 = %d         $a0 = %d \n" +
                "$a1 = %d         $a2 = %d         $a3 = %d         $t0 = %d\n" +
                "$t1 = %d         $t2 = %d         $t3 = %d         $t4 = %d\n" +
                "$t5 = %d         $t6 = %d         $t7 = %d         $s0 = %d\n" +
                "$s1 = %d         $s2 = %d         $s3 = %d         $s4 = %d\n" +
                "$s5 = %d         $s6 = %d         $s7 = %d         $t8 = %d\n" +
                "$t9 = %d         $sp = %d         $ra = %d\n\n", 
                registers[0], registers[2],registers[3], registers[4], 
                registers[5], registers[6], registers[7], registers[8], 
                registers[9], registers[10], registers[11], registers[12], 
                registers[13], registers[14], registers[15], registers[16], 
                registers[17], registers[18], registers[19], registers[20], 
                registers[21], registers[22], registers[23], registers[24], 
                registers[25], registers[29], ra);

                
            }
        }
    }

    public static ArrayList<String> getScripts(Scanner reader) {
        ArrayList<String> lst = new ArrayList<>();
        while(reader.hasNextLine()) {
            lst.add(reader.nextLine());
        }
        return lst;
    }

    public static ArrayList<Object> write(Hashtable<String, String> reg_codes, ArrayList<String> lines, Hashtable<String, String> label_addresses) {
        ArrayList<Object> instructions = new ArrayList<>();
        int pc = 0;
        for (String str : lines) {
            if (str.indexOf(':') != -1) {
                str = str.substring(str.indexOf(':')+1, str.length());
            }
            if (str.trim().isEmpty()) {
                continue;
            }
            String[] split = str.trim().split("[' '\\(\\)\\,\\$\\s]+");  
       
            if (split[0].equals("and")){
                String rd = reg_codes.get(split[1]);
                String rs = reg_codes.get(split[2]);
                String rt = reg_codes.get(split[3]);
                And curr = new And(rd, rs, rt);
                // curr.printObj();
                instructions.add(curr);

            }
            else if (split[0].equals("add")){
                String rd = reg_codes.get(split[1]);
                String rs = reg_codes.get(split[2]);
                String rt = reg_codes.get(split[3]);
                Add curr = new Add(rd, rs, rt);
                // curr.printObj();
                instructions.add(curr);
            }
            else if (split[0].equals("or")){
                String rd = reg_codes.get(split[1]);
                String rs = reg_codes.get(split[2]);
                String rt = reg_codes.get(split[3]);
                Or curr = new Or(rd, rs, rt);
                // curr.printObj();
                instructions.add(curr);
            }
            else if (split[0].equals("sub")){
                String rd = reg_codes.get(split[1]);
                String rs = reg_codes.get(split[2]);
                String rt = reg_codes.get(split[3]);
                Sub curr = new Sub(rd, rs, rt);
                // curr.printObj();
                instructions.add(curr);
            }
            else if (split[0].equals("sll")){
                String rd = reg_codes.get(split[1]);
                String rt = reg_codes.get(split[2]);
                String sa = Integer.toBinaryString(Integer.parseInt(split[3]));
                Sll curr = new Sll(rd, rt, sa);
                // curr.printObj();
                instructions.add(curr);
            }
            else if (split[0].equals("slt")){
                String rd = reg_codes.get(split[1]);
                String rs = reg_codes.get(split[2]);
                String rt = reg_codes.get(split[3]);
                Slt curr = new Slt(rd, rs, rt);
                // curr.printObj();
                instructions.add(curr);
            }
            else if (split[0].equals("jr")){
                String rs = reg_codes.get(split[1]);
                Jr curr = new Jr(rs);
                // curr.printObj();
                instructions.add(curr);
            }
            else if (split[0].equals("addi")){
                String rt = reg_codes.get(split[1]);
                String rs = reg_codes.get(split[2]);
                String temp = Integer.toBinaryString(Integer.parseInt(split[3]));
                String imm = changeBinLen(temp, 16, Integer.parseInt(split[3]));
                Addi curr = new Addi(rs, rt, imm);
                // curr.printObj();
                instructions.add(curr);
            }
            else if (split[0].equals("beq")){
                String rs = reg_codes.get(split[1]);
                String rt = reg_codes.get(split[2]);
                Integer address = Integer.parseInt(label_addresses.get(split[3]), 2);
                Integer num_offset = address - pc - 1;
                String bin_offset = Integer.toBinaryString(num_offset);
                String offset = changeBinLen(bin_offset, 16, num_offset);
                Beq curr = new Beq(rs, rt, offset);
                // curr.printObj();
                instructions.add(curr);
            }
            else if (split[0].equals("bne")){
                String rs = reg_codes.get(split[1]);
                String rt = reg_codes.get(split[2]);
                Integer address = Integer.parseInt(label_addresses.get(split[3]), 2);
                Integer num_offset = address - pc - 1;
                String bin_offset = Integer.toBinaryString(num_offset);
                String offset = changeBinLen(bin_offset, 16, num_offset);
                Bne curr = new Bne(rs, rt, offset);
                // curr.printObj();
                instructions.add(curr);
            }
            else if (split[0].equals("lw")){
                String rt = reg_codes.get(split[1]);
                String temp = Integer.toBinaryString(Integer.parseInt(split[2]));
                String offset = changeBinLen(temp, 16, Integer.parseInt(split[2]));
                String rs = reg_codes.get(split[3]);
                Lw curr = new Lw(rt, offset, rs);
                // curr.printObj();
                instructions.add(curr);
            }
            else if (split[0].equals("sw")){
                String rt = reg_codes.get(split[1]);
                String temp = Integer.toBinaryString(Integer.parseInt(split[2]));
                String offset = changeBinLen(temp, 16, Integer.parseInt(split[2]));
                String rs = reg_codes.get(split[3]);
                Sw curr = new Sw(rt, offset, rs);
                // curr.printObj();
                instructions.add(curr);
            }
            else if (split[0].equals("j")){
                String target = changeBinLen(label_addresses.get(split[1]), 26, 0);
                J curr = new J(target);
                // curr.printObj();
                instructions.add(curr);
            }
            else if (split[0].equals("jal")){
                String target = changeBinLen(label_addresses.get(split[1]), 26, 0);
                Jal curr = new Jal(target);
                // curr.printObj();
                instructions.add(curr);
            }
            else {
                System.out.println("invalid instruction: " + split[0]);
                break;
            }
            // System.out.println();
            pc += 1;
        }
        return instructions;
    }

    public static ArrayList<String> getLines(Scanner reader) throws IOException {
        ArrayList<String> lines = new ArrayList<String>();
        while (reader.hasNextLine()) {
            String line = reader.nextLine();
            if (line.indexOf('#') != -1)
                line = line.substring(0, line.indexOf('#'));
            else if (line.indexOf('#') == 0) {
                continue;
            } 
            if (!(line.trim().isEmpty())){
                lines.add(line);
            }
            
        }
        return lines;
    }

    public static Hashtable<String, String> buildLabelTable(ArrayList<String> lines){
        Hashtable<String, String> codes = new Hashtable<String, String>();
        for (int i = 0; i < lines.size(); i++){
            if (lines.get(i).indexOf(':') != -1){
                String binaryString = Integer.toBinaryString(i);
                int padding = 32 - binaryString.length();
                
                if (padding > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < padding; j++) {
                        sb.append('0');
                    }
                    sb.append(binaryString);
                    binaryString = sb.toString();
                }

                codes.put(lines.get(i).substring(0, lines.get(i).indexOf(':')), binaryString);
            }
        }
        return codes;
    }

    public static String changeBinLen(String binstr, int length, int num) {
        char add_char = '0';
        if (num < 0) {
            add_char = '1';
        }
        if (binstr.length() < length) {
            while (binstr.length() < length) {
                binstr = add_char + binstr;
            }
        } else if (binstr.length() > length) {
            while (binstr.length() > length) {
                binstr = binstr.substring(1);
            }
        }
        return binstr;
    }

    public static Hashtable<String, String> buildRegisterTable() {
        Hashtable<String, String> codes = new Hashtable<String, String>();
        // R-TYPE INSTRUCTIONS = OP CODES
        codes.put("zero", "00000");
        codes.put("0", "00000");

        codes.put("v0", "00010");
        codes.put("v1", "00011");
        
        codes.put("a0", "00100");
        codes.put("a1", "00101");
        codes.put("a2", "00110");
        codes.put("a3", "00111");
        
        codes.put("t0", "01000");
        codes.put("t1", "01001");
        codes.put("t2", "01010");
        codes.put("t3", "01011");
        codes.put("t4", "01100");
        codes.put("t5", "01101");
        codes.put("t6", "01110");
        codes.put("t7", "01111");
        codes.put("t8", "11000");
        codes.put("t9", "11001");
       
        codes.put("s0", "10000");
        codes.put("s1", "10001");
        codes.put("s2", "10010");
        codes.put("s3", "10011");
        codes.put("s4", "10100");
        codes.put("s5", "10101");
        codes.put("s6", "10110");
        codes.put("s7", "10111");

        codes.put("sp", "11101");
        codes.put("ra", "11111");
        return codes;
    }

// R type - and, or, add, sll, sub, slt, jr
// I Type - addi, beq, bne, lw, sw, 
// J Type - j, jal

}
 
