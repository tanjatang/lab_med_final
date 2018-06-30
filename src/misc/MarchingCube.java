/**
 * MarchingCube class for exercise 7
 * @author xiao; tang
 */

package misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
/*
 * medium point of edge (vertex1,vertex2)
 * e0(0,1)
 * e1(1,2)
 * e2(2,3)
 * e3(3,0)
 * e4(4,5)
 * e5(5,6)
 * e6(6,7)
 * e7(7,4)
 * e8(0,4)
 * e9(1,5)
 * e10(2,6)
 * e11(3,7)
 */
public class MarchingCube {
	final float SCALE = 128.0f;
	final double[] e0_x = {0f,-1*SCALE,1*SCALE};
	final double[] e1_x = {1*SCALE,0f,1*SCALE};
	final double[] e2_x = {0f,1*SCALE,1*SCALE};
	final double[] e3_x = {-1*SCALE,0f,1*SCALE};
	final double[] e4_x = {0f,-1*SCALE,-1*SCALE};
	final double[] e5_x = {1*SCALE,0f,-1*SCALE};
	final double[] e6_x = {0f,1*SCALE,-1*SCALE};
	final double[] e7_x = {-1*SCALE,0f,-1*SCALE};
	final double[] e8_x = {-1*SCALE,-1*SCALE,0f};
	final double[] e9_x = {1*SCALE,-1*SCALE,0f};
	final double[] e10_x = {1*SCALE,1*SCALE,0f};
	final double[] e11_x = {-1*SCALE,1*SCALE,0f};
	
	private HashMap<Integer,CubeCase> _table;
				
	public MarchingCube() {
		_table = new HashMap<Integer,CubeCase>();
		createTable();
	}
	public CubeCase get_Cube_From_Lib(int index) {
		return _table.get(index);
	}
	
	private void createTable() {
		Queue<CubeCase> basic_cube = new LinkedList<CubeCase>();
		 for(int i=1;i<=14;i++) {
			 CubeCase current = selectBasisCase(i);
			 basic_cube.offer(current);
			 basic_cube.offer(mirrorBasisCase(current));			 
		 }
		 
		 while(!basic_cube.isEmpty()) {
			 CubeCase[] current = new CubeCase[2];
			 CubeCase temp = basic_cube.poll();
			 current[0] = temp.rotate_left();
			 current[1] = temp.rotate_up();
			 for(CubeCase cur: current) {
				 if(!_table.containsKey((int)cur.get_vertex())) {
					 _table.put((int)cur.get_vertex(), cur);
					 basic_cube.offer(cur);
				 }
			 }			 
		 }		 
	}
	private CubeCase selectBasisCase(int index) {
		CubeCase current = new CubeCase(0, null);
		switch (index) {
		case 1:
			current = createBasicCase1();
			break;
		case 2:
			current = createBasicCase2();
			break;
		case 3:
			current = createBasicCase3();
			break;
		case 4:
			current = createBasicCase4();
			break;
		case 5:
			current = createBasicCase5();
			break;
		case 6:
			current = createBasicCase6();
			break;
		case 7:
			current = createBasicCase7();
			break;
		case 8:
			current = createBasicCase8();
			break;
		case 9:
			current = createBasicCase9();
			break;
		case 10:
			current = createBasicCase10();
			break;
		case 11:
			current = createBasicCase11();
			break;
		case 12:
			current = createBasicCase12();
			break;
		case 13:
			current = createBasicCase13();
			break;
		case 14:
			current = createBasicCase14();
			break;
		default:
			break;
		}
		return current;
	}
	private CubeCase mirrorBasisCase(CubeCase org) {
		byte index = (byte) (0xff^org.get_vertex());
		ArrayList<Triangle> list_triangle = new ArrayList<Triangle>();
		for(Triangle tri: org.get_Triangle()) {			
			double[] temp = tri.get_E(0);
			Triangle triangle = new Triangle(tri.get_E(1), 
					temp, tri.get_E(2));
			list_triangle.add(triangle);
		}
		CubeCase current = new CubeCase(index, list_triangle);
		return current;
	}
	
	private CubeCase createBasicCase1() {
		byte b = (byte)0x01;
		ArrayList<Triangle> tri= new ArrayList<Triangle>();
		Triangle t1 = new Triangle(e0_x, e8_x, e3_x);
		tri.add(t1);
		CubeCase basis = new CubeCase(b,tri);		
		return basis;
	}
	private CubeCase createBasicCase2() {
		byte b = (byte)0x03;
		ArrayList<Triangle> tri= new ArrayList<Triangle>();
		Triangle t1 = new Triangle(e3_x, e9_x, e8_x);
		Triangle t2 = new Triangle(e9_x, e3_x, e1_x);
		tri.add(t1);
		tri.add(t2);
		CubeCase basis = new CubeCase(b,tri);		
		return basis;
	}
	private CubeCase createBasicCase3() {
		byte b = (byte)0x05;
		ArrayList<Triangle> tri= new ArrayList<Triangle>();

		Triangle t1 = new Triangle(e0_x, e8_x, e3_x);
		Triangle t2 = new Triangle(e2_x, e10_x, e1_x);
		tri.add(t1);
		tri.add(t2);
		CubeCase basis = new CubeCase(b,tri);		
		return basis;
	}
	private CubeCase createBasicCase4() {
		byte b = (byte)0x32;
		ArrayList<Triangle> tri= new ArrayList<Triangle>();
		Triangle t1 = new Triangle(e0_x, e1_x, e8_x);
		Triangle t2 = new Triangle(e8_x, e1_x, e7_x);
		Triangle t3 = new Triangle(e7_x, e1_x, e5_x);
		tri.add(t1);
		tri.add(t2);
		tri.add(t3);
		CubeCase basis = new CubeCase(b,tri);		
		return basis;
	}
	private CubeCase createBasicCase5() {
		byte b = (byte)0x33;
		ArrayList<Triangle> tri= new ArrayList<Triangle>();

		Triangle t1 = new Triangle(e1_x, e7_x, e3_x);
		Triangle t2 = new Triangle(e7_x, e1_x, e5_x);
		tri.add(t1);
		tri.add(t2);
		CubeCase basis = new CubeCase(b,tri);		
		return basis;
	}
	private CubeCase createBasicCase6() {
		byte b = (byte)0x3A;
		ArrayList<Triangle> tri= new ArrayList<Triangle>();

		Triangle t1 = new Triangle(e0_x, e1_x, e8_x);
		Triangle t2 = new Triangle(e8_x, e1_x, e7_x);
		Triangle t3 = new Triangle(e7_x, e1_x, e5_x);
		Triangle t4 = new Triangle(e11_x, e2_x, e3_x);
		tri.add(t1);
		tri.add(t2);
		tri.add(t3);
		tri.add(t4);
		CubeCase basis = new CubeCase(b,tri);		
		return basis;
	}
	private CubeCase createBasicCase7() {
		byte b = (byte)0xA5;
		ArrayList<Triangle> tri= new ArrayList<Triangle>();

		Triangle t1 = new Triangle(e0_x, e8_x, e3_x);
		Triangle t2 = new Triangle(e9_x, e5_x, e4_x);
		Triangle t3 = new Triangle(e2_x, e10_x, e1_x);
		Triangle t4 = new Triangle(e6_x, e11_x, e7_x);
		tri.add(t1);
		tri.add(t2);
		tri.add(t3);
		tri.add(t4);
		CubeCase basis = new CubeCase(b,tri);		
		return basis;
	}
	private CubeCase createBasicCase8() {
		byte b = (byte)0xB1;
		ArrayList<Triangle> tri= new ArrayList<Triangle>();

		Triangle t1 = new Triangle(e3_x, e6_x, e11_x);
		Triangle t2 = new Triangle(e0_x, e6_x, e3_x);
		Triangle t3 = new Triangle(e6_x, e0_x, e5_x);
		Triangle t4 = new Triangle(e5_x, e0_x, e9_x);
		tri.add(t1);
		tri.add(t2);
		tri.add(t3);
		tri.add(t4);
		CubeCase basis = new CubeCase(b,tri);		
		return basis;
	}	
	private CubeCase createBasicCase9() {
		byte b = (byte)0xB2;
		ArrayList<Triangle> tri= new ArrayList<Triangle>();

		Triangle t1 = new Triangle(e0_x, e11_x, e8_x);
		Triangle t2 = new Triangle(e11_x, e0_x, e5_x);
		Triangle t3 = new Triangle(e11_x, e5_x, e6_x);
		Triangle t4 = new Triangle(e1_x, e5_x, e0_x);
		tri.add(t1);
		tri.add(t2);
		tri.add(t3);
		tri.add(t4);
		CubeCase basis = new CubeCase(b,tri);		
		return basis;
	}
	private CubeCase createBasicCase10() {
		byte b = (byte)0x41;
		ArrayList<Triangle> tri= new ArrayList<Triangle>();

		Triangle t1 = new Triangle(e0_x, e8_x, e3_x);
		Triangle t2 = new Triangle(e10_x, e6_x, e5_x);
		
		tri.add(t1);
		tri.add(t2);
		CubeCase basis = new CubeCase(b,tri);		
		return basis;
	}
	private CubeCase createBasicCase11() {
		byte b = (byte)0x43;
		ArrayList<Triangle> tri= new ArrayList<Triangle>();

		Triangle t1 = new Triangle(e3_x, e9_x, e8_x);
		Triangle t2 = new Triangle(e9_x, e3_x, e1_x);
		Triangle t3 = new Triangle(e10_x, e6_x, e5_x);
		tri.add(t1);
		tri.add(t2);			
		tri.add(t3);
		CubeCase basis = new CubeCase(b,tri);		
		return basis;
	}
	private CubeCase createBasicCase12() {
		byte b = (byte)0x4A;
		ArrayList<Triangle> tri= new ArrayList<Triangle>();

		Triangle t1 = new Triangle(e1_x, e9_x, e0_x);
		Triangle t2 = new Triangle(e11_x, e2_x, e3_x);
		Triangle t3 = new Triangle(e10_x, e6_x, e5_x);
		tri.add(t1);
		tri.add(t2);			
		tri.add(t3);
		CubeCase basis = new CubeCase(b,tri);		
		return basis;
	}
	private CubeCase createBasicCase13() {
		byte b = (byte)0x69;
		ArrayList<Triangle> tri= new ArrayList<Triangle>();

		Triangle t1 = new Triangle(e11_x, e0_x, e8_x);
		Triangle t2 = new Triangle(e0_x, e11_x, e2_x);
		Triangle t3 = new Triangle(e9_x, e6_x, e4_x);
		Triangle t4 = new Triangle(e6_x, e9_x, e10_x);
		tri.add(t1);
		tri.add(t2);			
		tri.add(t3);
		tri.add(t4);
		CubeCase basis = new CubeCase(b,tri);		
		return basis;
	}
	private CubeCase createBasicCase14() {
		byte b = (byte)0x71;
		ArrayList<Triangle> tri= new ArrayList<Triangle>();

		Triangle t1 = new Triangle(e7_x, e10_x, e6_x);
		Triangle t2 = new Triangle(e10_x, e7_x, e0_x);
		Triangle t3 = new Triangle(e10_x, e0_x, e9_x);
		Triangle t4 = new Triangle(e3_x, e0_x, e7_x);
		tri.add(t1);
		tri.add(t2);			
		tri.add(t3);
		tri.add(t4);
		CubeCase basis = new CubeCase(b,tri);		
		return basis;
	}

}
