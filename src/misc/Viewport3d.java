package misc;

import java.awt.*;

import java.util.Observable;
import java.util.Observer;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.universe.*;

import main.Message;
import main.Viewport;
import main.Segment;

import javax.media.j3d.*;
import javax.vecmath.Matrix3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

/**
 * Three dimensional viewport for viewing the dicom images + segmentations.
 * 
 * @author Karl-Ingo Friese
 */
@SuppressWarnings("serial")
public class Viewport3d extends Viewport implements Observer {
	/**
	 * Private class, implementing the GUI element for displaying the 3d data.
	 */
	public class Panel3d extends Canvas3D {
		public SimpleUniverse _simple_u;
		public BranchGroup _scene;

		public Panel3d(GraphicsConfiguration config) {
			super(config);
			setMinimumSize(new Dimension(DEF_WIDTH, DEF_HEIGHT));
			setMaximumSize(new Dimension(DEF_WIDTH, DEF_HEIGHT));
			setPreferredSize(new Dimension(DEF_WIDTH, DEF_HEIGHT));
			setBackground(Color.black);

			_simple_u = new SimpleUniverse(this);
			_simple_u.getViewingPlatform().setNominalViewingTransform();
			_scene = null;
			createScene();
			super.getView().setTransparencySortingPolicy(View.TRANSPARENCY_SORT_GEOMETRY);
		}

		public void createScene() {
			if (_scene != null) {
				_scene.detach();
			}
			// _scene = new BranchGroup();
			_scene = createContentBranch();
			//System.out.println("I am done!!!");
			_scene.setCapability(BranchGroup.ALLOW_DETACH);

			_scene.compile();
			_simple_u.addBranchGraph(_scene);
		}

	}

	private Panel3d _panel3d;
	private String _seg_name;
	private int _scale;
	private long _last_time;

	/**
	 * Constructor, with a reference to the global image stack as argument.
	 * 
	 * @param slices
	 *            a reference to the global image stack
	 */
	public Viewport3d() {
		super();
		_last_time = 0;
		_scale = 128;
		this.setPreferredSize(new Dimension(DEF_WIDTH, DEF_HEIGHT));
		this.setLayout(new BorderLayout());
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		_panel3d = new Panel3d(config);
		this.add(_panel3d, BorderLayout.CENTER);
	}

	/**
	 * calculates the 3d data structurs.
	 */
	public void update_view() {
		_panel3d.createScene();
	}

	/**
	 * Implements the observer function update. Updates can be triggered by the
	 * global image stack.
	 */
	public void update(final Observable o, final Object obj) {
		if (!EventQueue.isDispatchThread()) {
			// all swing thingies must be done in the AWT-EventQueue
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					update(o, obj);
				}
			});
			return;
		}

		// boolean update_needed = false;
		Message m = (Message) obj;

		if (m._type == Message.M_SEG_CHANGED) {
			String seg_name = ((Segment)m._obj).getName();
			boolean update_needed = _map_name_to_seg.containsKey(seg_name);
			long current_time = System.currentTimeMillis();
			if (update_needed) {//&&(current_time-_last_time>1000) 
				_seg_name = seg_name;				
				update_view();
			}
			_last_time = current_time;
		}
		if (m._type == Message.M_3D_SCALE) {
			int value = ((Integer)m._obj);
			_scale = value;
			update_view();
			System.out.println("scale3d: "+value);
		}
		
	}

	public Shape3D test() {
		GeometryArray points = new PointArray(3, PointArray.COORDINATES);
		Point3f p0 = new Point3f(-100f, 0f, 0f);
		Point3f p1 = new Point3f(100f, 0f, 0f);
		Point3f p2 = new Point3f(0f, 0f, 0f);
		points.setCoordinate(0, p0);
		points.setCoordinate(1, p1);
		points.setCoordinate(2, p2);

		PointAttributes pa = new PointAttributes();
		pa.setPointSize(10.0f);
		pa.setPointAntialiasingEnable(true);

		Appearance ap = new Appearance();
		ColoringAttributes color_ca = new ColoringAttributes(0, 1, 0, ColoringAttributes.FASTEST);
		ap.setColoringAttributes(color_ca);
		ap.setPointAttributes(pa);
		Shape3D three_points_shape = new Shape3D(points, ap);
		return three_points_shape;
	}
	public Shape3D createPointCloud(String seg_name) {
		if(seg_name==null) {
			//return test();
			System.out.println("createPointCloud:segname is null!");
			return new ColorCube(50);			
		}
		
		Segment seg = _map_name_to_seg.get(seg_name);
		System.out.println("createPointCloud!!!! "+seg.getName());

		BitMask[] bitmask = seg.get_bitMaskArray();

		
		Point3f[] points = new Point3f[113*256*256];
		int count = 0;
		for(int layer=0;layer<seg.getMaskNum();layer++) {
			for(int row=0;row<bitmask[0].get_h();row++) {
				for(int column=0;column<bitmask[0].get_w();column++) {
					if(bitmask[layer].get(column, row)==true) {
						points[count++] = 
								new Point3f(column-128,row-128,(layer-66)*2);
					}
				}
			}
		}

		// 换坐标显示
		/*		
		for(int layer=0;layer<seg.getMaskNum();layer++) {
			for(int row=0;row<bitmask[0].get_h();row++) {
				for(int column=0;column<bitmask[0].get_w();column++) {
					if(bitmask[layer].get(column, row)==true) {
						points.add(new Point3f(column,row,layer*2));
					}
				}
			}
		}
		 */		
		
		if(count==0)
			return null;

		GeometryArray geometrypoints = new PointArray(count, PointArray.COORDINATES);
		//System.out.println("length: "+count);
		geometrypoints.setCoordinates(0,points,0,count);
		PointAttributes pa = new PointAttributes(); // 定义点的特征 pa.setPointSize(5.0f);
		pa.setPointSize(1.0f);
		pa.setPointAntialiasingEnable(true);

		Appearance ap = new Appearance();
		ColoringAttributes color_ca = 
				new ColoringAttributes(1, 0, 0, ColoringAttributes.FASTEST);
		ap.setColoringAttributes(color_ca);
		ap.setPointAttributes(pa);
		Shape3D points_shape = new Shape3D(geometrypoints, ap);
		return points_shape;
	}
	
	public BranchGroup createContentBranch() {
		BranchGroup root = new BranchGroup();
		root.setCapability(BranchGroup.ALLOW_DETACH);

		Transform3D scale = new Transform3D();

		float[][] matrix = { { 1f, 0f, 0f }, { 0f, 0f, -1f }, { 0f, 1f, 0f }, };
		Matrix3f ma = new Matrix3f();
		for (int i = 0; i < 3; i++)
			ma.setRow(i, matrix[i]);
		Vector3d trans_vector = new Vector3d(-128 * (1.0f / 128), 128 * (1.0f / 128), -128 * (1.0f / 128));
//		scale.setTranslation(trans_vector);
		scale.setRotation(ma);
		scale.setScale(1.0f / _scale);
		TransformGroup tans = new TransformGroup(scale);
		tans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		root.addChild(tans);

		MouseRotate rotate = new MouseRotate();
		rotate.setTransformGroup(tans);
		rotate.setSchedulingBounds(new BoundingSphere(new Point3d(), 1000));
		tans.addChild(rotate);

//		ColorCube colorCube = new ColorCube(30);
//		tans.addChild(colorCube);
		Shape3D shape = createPointCloud(_seg_name);
		tans.addChild(shape);
		return root;
	}	
	
	public boolean toggleSeg(Segment seg) {
		String name = seg.getName();		
		if (_map_name_to_seg.get(name)!=null) {
			_map_name_to_seg.remove(name);
			_seg_name = null;
			System.out.println("toggleSeg: remove");
		} else {			
				_map_name_to_seg.put(name, seg);
				_seg_name = seg.getName();
				System.out.println("toggleSeg: add "+_seg_name);											
		}
		boolean gotcha = _map_name_to_seg.containsKey(name);
		update_view();
		return gotcha;
	}

}
