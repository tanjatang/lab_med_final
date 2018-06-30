/**
 * CubeCase class for exercise 7
 * @author xiao; tang
 */

package misc;
import java.util.ArrayList;
import java.util.List;

public class CubeCase {
	//0x 0 0  0  0  0  0  0  0
	//  v7 v6 v5 v4 v3 v2 v1 v0
	byte _vertex;
	List<Triangle> _triangle_list;

	
	private boolean checkRealPoint(int index) {
		if(((get_vertex()>>index)&1) == 1)
			return true;
		else
			return false;
	}

	public CubeCase(int vertex, List<Triangle> triangle) {
		// TODO Auto-generated constructor stub
		_vertex = (byte)vertex;
		_triangle_list = triangle;							
	}

	
	public CubeCase rotate_left() {
		CubeCase current = new CubeCase(0x00, null);
		byte old_vertex = _vertex;
		byte new_vertex = 0x00;
		new_vertex +=(byte) ((old_vertex & 0x02)>>1);
		new_vertex +=(byte) ((old_vertex & 0x20)>>4);
		new_vertex +=(byte) ((old_vertex & 0x40)>>4);
		new_vertex +=(byte) ((old_vertex & 0x04)<<1);
		new_vertex +=(byte) ((old_vertex & 0x01)<<4);
		new_vertex +=(byte) ((old_vertex & 0x10)<<1);
		new_vertex +=(byte) ((old_vertex & 0x80)>>1);
		new_vertex +=(byte) ((old_vertex & 0x08)<<4);
		
		ArrayList<Triangle> cur_coor = new ArrayList();
		for(Triangle temp :this._triangle_list) {
			cur_coor.add(temp.rot_left());
		}		
		current.set_vertex(new_vertex);
		current.set_Triangle(cur_coor);
		return current;		
	}
	
	public CubeCase rotate_up() {
		CubeCase current = new CubeCase(0x00, null);
		byte old_vertex = _vertex;
		byte new_vertex = 0x00;
		new_vertex +=(byte) ((old_vertex & 0x10)>>4);//
		new_vertex +=(byte) ((old_vertex & 0x20)>>4);//
		new_vertex +=(byte) ((old_vertex & 0x02)<<1);//
		new_vertex +=(byte) ((old_vertex & 0x01)<<3);//
		new_vertex +=(byte) ((old_vertex & 0x80)>>3);//
		new_vertex +=(byte) ((old_vertex & 0x40)>>1);//
		new_vertex +=(byte) ((old_vertex & 0x04)<<4);
		new_vertex +=(byte) ((old_vertex & 0x08)<<4);
		
		ArrayList<Triangle> cur_coor = new ArrayList();
		for(Triangle temp :this._triangle_list) {
			cur_coor.add(temp.rot_up());
		}		
		current.set_vertex(new_vertex);
		current.set_Triangle(cur_coor);
		return current;		
	}
	
	public int get_vertex() {
		return _vertex&0xff;
	}

	public void set_vertex(byte vertex) {
		this._vertex = vertex;
	}
	
	public List<Triangle> get_Triangle() {
		return _triangle_list;
	}

	public void set_Triangle(List<Triangle> _Triangle) {
		this._triangle_list = _Triangle;
	}

}
