/**
 * Triangle class for exercise 7
 * @author xiao; tang
 */

package misc;
import misc.Triangle;

public class Triangle {
	private float[] _e0;
	private float[] _e1;
	private float[] _e2;

	public Triangle(float[] e0,float[] e1,float[] e2) {
		// TODO Auto-generated constructor stub
		_e0 = e0;
		_e1 = e1;
		_e2 = e2;
	}	
	public void set_E(int i,float[] value) {
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
	public float[] get_E(int i) {
		if(i==0)
			return _e0;
		else if(i==1)
			return _e1;
		else 
			return _e2;
	} 
	

}
