/**
 * CubeCase class for exercise 7
 * @author xiao; tang
 */

package misc;


public class CubeCase {
	//0x 0 0  0  0  0  0  0  0
	//  v7 v6 v5 v4 v3 v2 v1 v0
	byte _vertex;
//	List<Triangle> _triangle_list;
	Triangle[] _triangle_list_new;

	

	public CubeCase(int vertex, Triangle[] triangle) {
		// TODO Auto-generated constructor stub
		_vertex = (byte)vertex;
//		_triangle_list = triangle;
		
		_triangle_list_new = triangle;
	}

	
	
	public int get_vertex() {
		return _vertex&0xff;
	}

	public void set_vertex(byte vertex) {
		this._vertex = vertex;
	}
	
	public Triangle[] get_Triangle() {
		return _triangle_list_new;
	}

	public void set_Triangle(Triangle[] Triangle) {
		this._triangle_list_new = Triangle;
	}

}
