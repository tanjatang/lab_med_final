package misc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Observable;
import java.util.Observer;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.universe.*;

import main.LabMed;
import main.Message;
import main.Viewport;
import main.Viewport2d;
import main.Segment;

import javax.media.j3d.*;
import javax.vecmath.Color4b;
import javax.vecmath.Matrix3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;
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

	public Viewport2d _view2d;
	private Panel3d _panel3d;
	private String _seg_name;
	private int _scale;
	Matrix3f _matrix;
	private int _xaxis,_yaxis,_zaxis;
	private long _last_time;

	/**
	 * Constructor, with a reference to the global image stack as argument.
	 * 
	 * @param slices
	 *            a reference to the global image stack
	 */
	public Viewport3d() {
		super();
		_view2d = LabMed.get_v2d();
		_last_time = 0;
		_scale = 200;
		_matrix = new Matrix3f();
		float[][] ma = { { 1f, 0f, 0f }, 
				 { 0f, 0f, -1f }, 
				 { 0f, 1f, 0f }, };
		//Matrix3f matrix = new Matrix3f();
		for (int i = 0; i < 3; i++)
			_matrix.setRow(i, ma[i]);
		_xaxis = 0;
		_yaxis = 0;
		_zaxis = 0;
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
			int[] value = ((int[])m._obj);
			_xaxis = value[0];
			_yaxis = value[1];
			_zaxis = value[2];
			_scale = value[3];
			update_view();
		}
		
		if (m._type == Message.M_NEW_ACTIVE_IMAGE) {
			update_view();			
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
			//return new ColorCube(50);	
			return null;
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

		Vector3d trans_vector = new Vector3d(-128 * (1.0f / 128), 128 * (1.0f / 128), -128 * (1.0f / 128));
//		scale.setTranslation(trans_vector);
		
		scale.setRotation(_matrix);
		
		scale.getRotationScale(_matrix);
		scale.setScale(1.0f / _scale);
		TransformGroup tans = new TransformGroup(scale);
		tans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		root.addChild(tans);

		MouseRotate rotate = new MouseRotate();
		rotate.setTransformGroup(tans);
		rotate.setSchedulingBounds(new BoundingSphere(new Point3d(), 1000));
		tans.addChild(rotate);

		Shape3D shape = createPointCloud(_seg_name);
		if(shape==null) {			
			tans.addChild(orthoSlices());
		}else {
			tans.addChild(shape);
		}
		tans.addChild(cubeShape());
		tans.addChild(textTest());
			
		return root;
	}	
	
	private Shape3D orthoSlices() {
		int x=0;
		int y=0;
		int z=0;
		int active_id = _slices.getActiveImageID();
		byte[] bs =  new byte[4];
		if(_view2d.get_slice_names().isEmpty()) {
			x=0;y=0;z=0;
		}else {
			Point3d p0 = new Point3d();
			Point3d p1 = new Point3d();
			Point3d p2 = new Point3d();
			Point3d p3 = new Point3d();
			Color4b color = new Color4b();
			QuadArray sq = new QuadArray(12, QuadArray.COORDINATES|GeometryArray.TEXTURE_COORDINATE_2
					| GeometryArray.COLOR_4);
			
			switch (_view2d.get_viewmodel()) {
			case 0:
				z = active_id*2-112;
				p0.set(-128, -128, z);
				p1.set(128, -128, z);
				p2.set(128, 128, z);
				p3.set(-128, 128, z);
				final byte[] bs1= {(byte)50,(byte)0,(byte)0,(byte)255};
				bs = bs1;			
				break;
			case 1:
				y = active_id-128;
				p0.set(-128, y, -128);
				p1.set(128, y, -128);
				p2.set(128, y, 128);
				p3.set(-128, y, 128);
				final byte[] bs2= {(byte)0, (byte)50, (byte)0, (byte)255};
				bs = bs2;
				break;
			case 2:
				x = active_id-128;
				p0.set(x, -128, -128);
				p1.set(x, 128, -128);
				p2.set(x, 128, 128);
				p3.set(x, -128, 128);
				final byte[] bs3= {(byte)0, (byte)0, (byte)50, (byte)255};
				bs = bs3;
				break;
			default:
				break;
			}
			color.set(bs);
			sq.setCoordinate(0, p0);
			sq.setCoordinate(1, p1);
			sq.setCoordinate(2, p2);
			sq.setCoordinate(3, p3);
			sq.setColor(0, color);
			sq.setColor(1, color);
			sq.setColor(2, color);
			sq.setColor(3, color);
			sq.setTextureCoordinate(0, 0,new TexCoord2f(0.0f,0.0f));
			sq.setTextureCoordinate(0, 1,new TexCoord2f(1.0f,0.0f));
			sq.setTextureCoordinate(0, 2,new TexCoord2f(1.0f,1.0f));
			sq.setTextureCoordinate(0, 3,new TexCoord2f(0.0f,1.0f));

			
			BufferedImage img = _view2d.getBGImage(_view2d.get_viewmodel(), _slices.getActiveImageID());
			ImageComponent2D i2d = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA, img);
			Texture2D tex = new Texture2D(Texture2D.BASE_LEVEL,Texture2D.RGBA,img.getWidth(),img.getHeight());
			tex.setImage(0, i2d);
			
			Appearance ap_plane = new Appearance();
			PolygonAttributes pa = new PolygonAttributes();
			pa.setPolygonMode(PolygonAttributes.POLYGON_FILL);
			pa.setCullFace(PolygonAttributes.CULL_NONE);
			
			ap_plane.setPolygonAttributes(pa);
			ap_plane.setTexture(tex);
			
			Shape3D shape = new Shape3D(sq,ap_plane);
			return shape;
		}
		
		return new Shape3D();
	}

	private Shape3D cubeShape() {
		Point3d p0 = new Point3d(-128, -128, 128);
		Point3d p1 = new Point3d(-128, 128, 128);
				
		Point3d p2 = new Point3d(128, -128, 128);
		Point3d p3 = new Point3d(128, 128, 128);
		
		Point3d p4 = new Point3d(-128, -128, -128);
		Point3d p5 = new Point3d(-128, 128, -128);
				
		Point3d p6 = new Point3d(128, -128, -128);
		Point3d p7 = new Point3d(128, 128, -128);
		
		Point3d p00 = new Point3d(-128, 128, 128);
		Point3d p10 = new Point3d(128, 128, 128);
				
		Point3d p20 = new Point3d(-128, -128, 128);
		Point3d p30 = new Point3d(128, -128, 128);
		
		Point3d p40 = new Point3d(-128, -128, -128);
		Point3d p50 = new Point3d(128, -128, -128);
				
		Point3d p60 = new Point3d(-128, 128, -128);
		Point3d p70 = new Point3d(128, 128, -128);
		
		Point3d p01 = new Point3d(-128, -128, -128);
		Point3d p11 = new Point3d(-128, -128, 128);
				
		Point3d p21 = new Point3d(128, 128, -128);
		Point3d p31 = new Point3d(128, 128, 128);
		
		Point3d p41 = new Point3d(-128, 128, -128);
		Point3d p51 = new Point3d(-128, 128, 128);
				
		Point3d p61 = new Point3d(128, -128, -128);
		Point3d p71 = new Point3d(128, -128, 128);
		
		Point3d p02 = new Point3d(-128, 0, 0);
		Point3d p12 = new Point3d(128, 0, 0);
		Point3d p22 = new Point3d(0, -128, 0);
		Point3d p32 = new Point3d(0, 128, 0);
		Point3d p42 = new Point3d(0, 0, -128);
		Point3d p52 = new Point3d(0, 0, 128);
		
		LineArray la = new LineArray(30, LineArray.COORDINATES);
		la.setCoordinate(0, p0);
		la.setCoordinate(1, p1);
		la.setCoordinate(2, p2);
		la.setCoordinate(3, p3);
		la.setCoordinate(4, p4);
		la.setCoordinate(5, p5);
		la.setCoordinate(6, p6);
		la.setCoordinate(7, p7);
		
		la.setCoordinate(8, p00);
		la.setCoordinate(9, p10);
		la.setCoordinate(10, p20);
		la.setCoordinate(11, p30);
		la.setCoordinate(12, p40);
		la.setCoordinate(13, p50);
		la.setCoordinate(14, p60);
		la.setCoordinate(15, p70);
		
		la.setCoordinate(16, p01);
		la.setCoordinate(17, p11);
		la.setCoordinate(18, p21);
		la.setCoordinate(19, p31);
		la.setCoordinate(20, p41);
		la.setCoordinate(21, p51);
		la.setCoordinate(22, p61);
		la.setCoordinate(23, p71);
		
		la.setCoordinate(24, p02);
		la.setCoordinate(25, p12);
		la.setCoordinate(26, p22);
		la.setCoordinate(27, p32);
		la.setCoordinate(28, p42);
		la.setCoordinate(29, p52);
		
		
		LineAttributes linea = new LineAttributes(); 
		linea.setLineWidth(1.0f);
		linea.setLineAntialiasingEnable(true);
		
		Appearance ap = new Appearance();
		ColoringAttributes color_ca = new ColoringAttributes(1, 1, 1, ColoringAttributes.FASTEST);
		ap.setColoringAttributes(color_ca);
		ap.setLineAttributes(linea);
		
		Shape3D line = new Shape3D(la,ap);
		return line;
	}
	
	private Shape3D textTest() {
		Point3f p1 = new Point3f(100, 0, 0);
		Point3f p2 = new Point3f(0, 100, 0);
		Font font = new Font("TimesRoman",Font.PLAIN,20);
		Font3D font3D = new Font3D(font,new FontExtrusion());
		
		Text3D text3Dx = new Text3D(font3D, "X", p1);
		Text3D text3Dy = new Text3D(font3D, "Y", p2);
		Appearance ap = new Appearance();
		ColoringAttributes color_ca = new ColoringAttributes(0, 0, 1, ColoringAttributes.FASTEST);
		ap.setColoringAttributes(color_ca);
		Shape3D shape3d = new Shape3D(text3Dx, ap);
		shape3d.addGeometry(text3Dy);
		return shape3d;
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
