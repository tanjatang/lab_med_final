/**
 * Triangle class for exercise 7
 * @author xiao; tang
 */

package misc;
import misc.Triangle;

public class Triangle {
	private double[] _e0;
	private double[] _e1;
	private double[] _e2;
	final double[][] ROT_matrix_left = {{0f,0f,-1f},
	   									{0f,1f,0f},
	   									{1f,0f,0f}};
	final double[][] ROT_matrix_up = {{1f,0f,0f},
									  {0f,0f,1f},
									  {0f,-1f,0f}};

	public Triangle(double[] e0,double[] e1,double[] e2) {
		// TODO Auto-generated constructor stub
		_e0 = e0;
		_e1 = e1;
		_e2 = e2;
	}	
	public void set_E(int i,double[] value) {
		switch (i) {
		case 0:
			_e0 = value;
			break;		
		case 1:
			_e1 = value;
			break;
		case 2:
			_e2 = value;
			break;		
		default:
			break;
		}
	}
	public double[] get_E(int i) {
		if(i==0)
			return _e0;
		else if(i==1)
			return _e1;
		else 
			return _e2;
	} 
	
	public Triangle rot_left() {		
		Triangle current = new Triangle(new double[3], new double[3], new double[3]);
		for(int i=0;i<3;i++)
			current.set_E(i, multiplicationOfMatrix(ROT_matrix_left,this.get_E(i)));
		return current;
	}
	public Triangle rot_up() {		
		Triangle current = new Triangle(new double[3], new double[3], new double[3]);
		for(int i=0;i<3;i++)
			current.set_E(i, multiplicationOfMatrix(ROT_matrix_up,this.get_E(i)));
		return current;
	}
	
 	public double[] multiplicationOfMatrix(double[][] matrix,double[] coordinate) {
		int dimM = coordinate.length;
		double[] tmp = new double[dimM];
		try {
			for(int i=0;i<dimM;i++) {
				double sum = 0;
				for(int j=0;j<dimM;j++) {
					sum = sum+ matrix[i][j] * coordinate[j];
				}
				tmp[i] = sum;
			}
		}catch(ArrayIndexOutOfBoundsException e){
			System.out.println("Exception thrown  :" + e);
		}
		return tmp;
	}
}
