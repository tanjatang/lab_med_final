package misc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseWheelZoom;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.universe.*;

import com.sun.j3d.utils.geometry.Sphere;
import main.LabMed;
import main.Message;
import main.Viewport;
import main.Viewport2d;
import main.Segment;

import javax.media.j3d.*;
import javax.vecmath.Color3b;
import javax.vecmath.Color3f;
import javax.vecmath.Color4b;
import javax.vecmath.Color4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Point4d;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * Three dimensional viewport for viewing the dicom images + segmentations.
 * 
 * @author Karl-Ingo Friese
 */
@SuppressWarnings("serial")
public class Viewport3d extends Viewport implements Observer {
	
	public Viewport2d _view2d;
	private Panel3d _panel3d;
	private String _seg_name;
	private int _scale;
	private int _size_cube;
	private int _xaxis,_yaxis,_zaxis;
	public MarchingCube _marchingCube;
	private int[] _idxs;
	private long _old_time;
	private int _view_model;
	final int DEFAULT_CASE = 0;
	final int TEXTURE_2D = 1;
	final int POINT_CLOUD = 2;
	final int MARCHING_CUBE = 3;
	Matrix3f _matrix;
	Shape3D _marching_cube_shape;
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
			switch (_view_model) {
			case DEFAULT_CASE:
				_scene = defaultBranchGroupe();
				break;
			case TEXTURE_2D:
				_scene = texture2dBranchGroupe();

				break;
			case POINT_CLOUD:
				_scene = pointCloudBranchGroupe();
				break;
			case MARCHING_CUBE:
				_scene = marchingCubeBranchGroupe();				
				break;
			default:
				break;
			}
			_scene.setCapability(BranchGroup.ALLOW_DETACH);
			_scene.compile();
			_simple_u.addBranchGraph(_scene);	
			
		}

	}

	/**
	 * Constructor, with a reference to the global image stack as argument.
	 * 
	 * @param slices
	 *            a reference to the global image stack
	 */
	public Viewport3d() {
		super();
		_view2d = LabMed.get_v2d();
		_scale = 200;
		_size_cube = 1;
		_matrix = new Matrix3f();
		float[][] ma = { { 1f, 0f, 0f }, 
				         { 0f, 0f, -1f }, 
				         { 0f, 1f, 0f }, };
		
		for (int i = 0; i < 3; i++)
			_matrix.setRow(i, ma[i]);
		_xaxis = 0;
		_yaxis = 0;
		_zaxis = 0;
		_marchingCube = new MarchingCube(1.0f);
//		_marchingCube.get_Cube_From_Lib(0xfe);
		
		this.setPreferredSize(new Dimension(DEF_WIDTH, DEF_HEIGHT));
		this.setLayout(new BorderLayout());
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		_panel3d = new Panel3d(config);
		this.add(_panel3d, BorderLayout.CENTER);
				
		_idxs = new int[256*256*113];
		for(int i=0;i<_idxs.length;i++)
			_idxs[i] = i;
		
		_old_time = 0;
		_view_model = 0;
		_marching_cube_shape = createText("no segment currently",_scale);
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
			if (update_needed) {//&&(current_time-_last_time>1000) 
				_seg_name = seg_name;				
				update_view();
			}
		}
		if (m._type == Message.M_3D_SCALE) {
			int[] value = ((int[])m._obj);
			_xaxis = value[0];
			_yaxis = value[1];
			_zaxis = value[2];
			_scale = value[3];
			_size_cube = value[4];
			update_view();
		}
		
		if (m._type == Message.M_NEW_ACTIVE_IMAGE||
				m._type == Message.M_NEW_SETTING||
				m._type == Message.M_SEG_CHANGED) {
			update_view();			
		}
	}

	public Shape3D createPointCloud(Segment seg) {
		if(seg.getName()==null) {
			return null;
		}
		
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

	
	
	public BranchGroup texture2dBranchGroupe() {		
		
		BranchGroup root = new BranchGroup();
		root.setCapability(BranchGroup.ALLOW_DETACH);
		Transform3D scale = new Transform3D();
		
		_matrix = calculateFinalRotationMatrix(_xaxis/100f,_yaxis/100f,_zaxis/100f);
		scale.setRotation(_matrix);
		scale.setScale(1.0f / _scale);
		TransformGroup tans = new TransformGroup(scale);
		tans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		root.addChild(tans);

		MouseRotate rotate = new MouseRotate();
		rotate.setTransformGroup(tans);
		rotate.setSchedulingBounds(new BoundingSphere(new Point3d(), 1000));
		tans.addChild(rotate);

		Shape3D orthoslices = orthoSlices();
		tans.addChild(orthoslices);
		tans.addChild(cubeShape(128));
		tans.addChild(createCoordinate('x', 128));
		tans.addChild(createChar('x', 120));
		tans.addChild(createCoordinate('y', 128));
		tans.addChild(createChar('y', 120));
		tans.addChild(createCoordinate('z', 128));
		tans.addChild(createChar('z', 120));
		
		return root;
	}
	public BranchGroup pointCloudBranchGroupe() {
		BranchGroup root = new BranchGroup();
		root.setCapability(BranchGroup.ALLOW_DETACH);

		Transform3D scale = new Transform3D();		
		_matrix = calculateFinalRotationMatrix(_xaxis/100f,_yaxis/100f,_zaxis/100f);
		if(!_map_name_to_seg.isEmpty())
			scale.setRotation(_matrix);
		scale.setScale(1.0f / _scale);
		TransformGroup tans = new TransformGroup(scale);
		tans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		root.addChild(tans);

		MouseRotate rotate = new MouseRotate();
		rotate.setTransformGroup(tans);
		rotate.setSchedulingBounds(new BoundingSphere(new Point3d(), 1000));
		tans.addChild(rotate);
		
		Shape3D shape = new Shape3D();
		if(_map_name_to_seg.isEmpty()) {
			shape = createText("no segment currently",_scale);
		}else {
			String name = null;
			for(String seg_name : _map_name_to_seg.keySet()) {		
				name = seg_name;
			}		
			shape = createPointCloud(_map_name_to_seg.get(name));		
		}
		tans.addChild(shape);
		tans.addChild(cubeShape(128));
		
		return root;
	}

	public BranchGroup marchingCubeBranchGroupe() {
		BranchGroup root = new BranchGroup();
		root.setCapability(BranchGroup.ALLOW_DETACH);

		Transform3D scale = new Transform3D();
		
		_matrix = calculateFinalRotationMatrix(_xaxis/100f,_yaxis/100f,_zaxis/100f);
		if(!_map_name_to_seg.isEmpty())
			scale.setRotation(_matrix);
		scale.setScale(1.0f / _scale);
		TransformGroup tans = new TransformGroup(scale);
		tans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		root.addChild(tans);

		MouseRotate rotate = new MouseRotate();
		rotate.setTransformGroup(tans);
		rotate.setSchedulingBounds(new BoundingSphere(new Point3d(), 1000));
		tans.addChild(rotate);
		
		MouseWheelZoom mouseWheelZoom = new MouseWheelZoom();
		mouseWheelZoom.setTransformGroup(tans);
		mouseWheelZoom.setSchedulingBounds(new BoundingSphere(new Point3d(), 1000));		
		tans.addChild(mouseWheelZoom);		
		Shape3D temp = new Shape3D(_marching_cube_shape.getGeometry(),_marching_cube_shape.getAppearance());
		tans.addChild(temp);
		tans.addChild(cubeShape(128));

		Color3f light1Color = new Color3f(1.0f, 1.0f, 0.0f);
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.MAX_VALUE);
		AmbientLight a_Light = new AmbientLight();
		Vector3f dir = new Vector3f(-1f, 0.0f, -1.0f);
		dir.normalize();
		DirectionalLight d_light = new DirectionalLight();
		d_light.setInfluencingBounds(bounds);
		d_light.setColor(new Color3f(1.0f, 1.0f, 1.0f));
		d_light.setDirection(dir);
		a_Light.setInfluencingBounds(bounds);
		a_Light.setColor(light1Color);
		root.addChild(d_light);
		root.addChild(a_Light);		
		
		return root;
	}
	
	public BranchGroup defaultBranchGroupe() {
		
		BranchGroup root = new BranchGroup();
		root.setCapability(BranchGroup.ALLOW_DETACH);
		
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.MAX_VALUE);
		Color3f light1Color = new Color3f(0.0f, 0.0f, 1.0f);
		AmbientLight a_Light = new AmbientLight();
		a_Light.setInfluencingBounds(bounds);
		a_Light.setColor(light1Color);
		root.addChild(a_Light);  //背景光
		
		Vector3f dir = new Vector3f(-1.0f, -1.0f, 0.0f);
		dir.normalize();
		DirectionalLight d_light = new DirectionalLight();
		d_light.setInfluencingBounds(bounds);
		d_light.setColor(new Color3f(1.0f, 1.0f, 1.0f));
		d_light.setDirection(dir);
		root.addChild(d_light); // 向左下的平行光
//		root.addChild(createCoordinate('x',1));
//		root.addChild(createCoordinate('y',1));
//		root.addChild(createCoordinate('z',1));
		Sphere sphere = new Sphere(0.4f,1,100);		
		root.addChild(sphere);
//----------------------------------------------------------------
		
		Transform3D rotation_shpere = new Transform3D();

		TransformGroup tansformgroup = new TransformGroup(rotation_shpere);
		tansformgroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);		
		root.addChild(tansformgroup);					
		MouseRotate rotate = new MouseRotate();
		rotate.setTransformGroup(tansformgroup);
		rotate.setSchedulingBounds(new BoundingSphere(new Point3d(), 1000));

		MouseWheelZoom mouseWheelZoom = new MouseWheelZoom();
		mouseWheelZoom.setTransformGroup(tansformgroup);
		mouseWheelZoom.setSchedulingBounds(new BoundingSphere(new Point3d(), 1000));		
		tansformgroup.addChild(rotate);
		tansformgroup.addChild(mouseWheelZoom);		

		  	Transform3D scale_new = new Transform3D();
			final float[] tr= {0.7f,0.6f,0f};
			scale_new.setTranslation(new Vector3f(tr));
			Sphere sphere1 = new Sphere(0.1f,1,100);
			
			Appearance ap_plane = new Appearance();
			ap_plane.setMaterial(new Material());
			ColoringAttributes color_ca = new ColoringAttributes(0f, 0f, 1f, ColoringAttributes.FASTEST);
			ap_plane.setColoringAttributes(color_ca);
			sphere1.setAppearance(ap_plane);
			
			TransformGroup tansform_new = new TransformGroup(scale_new);
			tansform_new.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

			MouseRotate rotate_n = new MouseRotate();
			rotate_n.setTransformGroup(tansform_new);
			rotate_n.setSchedulingBounds(new BoundingSphere(new Point3d(), 1000));
			tansform_new.addChild(rotate_n);
			tansform_new.addChild(sphere1);
			
		tansformgroup.addChild(tansform_new);	

		return root;
	}	
	
	private Shape3D createText(String str,int scale) {
		Point3f point = new Point3f(-100f, 0, 0);
		Font font = new Font("TimesRoman",Font.PLAIN,1+(int)(scale/8f));
		Font3D font3D = new Font3D(font,new FontExtrusion());
		Text3D text3D = new Text3D(font3D, str, point);
		
		Appearance ap = new Appearance();
		ColoringAttributes color_ca = new ColoringAttributes(0.2f, 0.5f, 1, ColoringAttributes.FASTEST);
		ap.setColoringAttributes(color_ca);
		TransparencyAttributes trana = new TransparencyAttributes(TransparencyAttributes.FASTEST,0.8f);
		ap.setTransparencyAttributes(trana);
		Shape3D shape3d = new Shape3D(text3D, ap);
		return shape3d;
	}
	/*
	 *  generate one shape3d of Cubecase
	 */
	private Shape3D segTrias(Segment seg,int s_size) {
		Point3f[] seg_trias = new Point3f[256*256*113];
		int zmax = seg.getMaskNum();
		int ymax = seg.getMask(0).get_h();
		int xmax = seg.getMask(0).get_w();
		int count = 0;
		
		for(int z=0;z<zmax-s_size;z+=s_size) {
			BitMask mask1 = seg.getMask(z);
			BitMask mask2 = seg.getMask(z+s_size);
			for(int y=0;y<ymax-s_size;y+=s_size) 
				for(int x=0;x<xmax-s_size;x+=s_size) {
					int index = calculate_index(x,y,s_size,mask1,mask2);
					if(index!=0&&index!=0xff) {
						Triangle[] mtrias = _marchingCube.get_Cube_From_Lib(index).get_Triangle();
						for(Triangle tria:mtrias) {
							Point3f trans = new Point3f(x-128,y-128,(z-66)*2);
							Point3f a = new Point3f(tria.get_E(0));
							Point3f b = new Point3f(tria.get_E(1));
							Point3f c = new Point3f(tria.get_E(2));
							
							a.scale(s_size);
							b.scale(s_size);
							c.scale(s_size);
							a.add(trans);
							b.add(trans);
							c.add(trans);
							seg_trias[count++] = a;
							seg_trias[count++] = b;
							seg_trias[count++] = c;
						}
					}
			}
		}
		
		IndexedTriangleArray itrias = new IndexedTriangleArray(count, IndexedTriangleArray.COORDINATES|
				IndexedTriangleArray.NORMALS, count);
		itrias.setCoordinates(0, seg_trias,0,count);
		
		int [] targetArray = new int [count];
		System.arraycopy(_idxs, 0, targetArray, 0, count);
		itrias.setCoordinateIndices(0, targetArray);
//		for(int i=0;i<count;i++) {
//			itrias.setCoordinate(i, seg_trias[i]);
//			itrias.setCoordinateIndex(i, i);
//		}
		 final GeometryInfo gi = new GeometryInfo(itrias);
			 final NormalGenerator normalGenerator = new NormalGenerator();
			 normalGenerator.generateNormals(gi);
			 final GeometryArray geometryArray = gi.getGeometryArray();
			 {
			 geometryArray.setCapability(GeometryArray.ALLOW_COLOR_READ);
			 geometryArray.setCapability(GeometryArray.ALLOW_COLOR_WRITE);
			 }
			 
			 
		Appearance ap_plane = new Appearance();
		ap_plane.setMaterial(new Material());
		ColoringAttributes color_ca = new ColoringAttributes(1f, 0f, 0f, ColoringAttributes.FASTEST);
		ap_plane.setColoringAttributes(color_ca);	
		
		Shape3D square = new Shape3D(geometryArray,ap_plane);
		return square;
	}
	
	private int calculate_index(int x,int y,int size,BitMask mask1,BitMask mask2) {
//		boolean[] v = new boolean[8];
		int[] v = new int[8];
		v[0] = mask2.get(x, y) ?1:0;
		v[1] = mask2.get(x+size, y) ?1:0;
		v[2] = mask2.get(x+size, y+size) ?1:0;
		v[3] = mask2.get(x, y+size) ?1:0;
		v[4] = mask1.get(x, y) ?1:0;
		v[5] = mask1.get(x+size, y) ?1:0;
		v[6] = mask1.get(x+size, y+size) ?1:0;
		v[7] = mask1.get(x, y+size) ?1:0;
		byte index = 0;
		for(int i=0;i<8;i++) {
				index += v[i]<<i;
			}		
		return index&0xff;
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
			QuadArray sq = new QuadArray(12, QuadArray.COORDINATES|GeometryArray.TEXTURE_COORDINATE_2
					);//| GeometryArray.COLOR_4
			
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
			sq.setCoordinate(0, p0);
			sq.setCoordinate(1, p1);
			sq.setCoordinate(2, p2);
			sq.setCoordinate(3, p3);
			sq.setTextureCoordinate(0, 0,new TexCoord2f(0.0f,0.0f));
			sq.setTextureCoordinate(0, 1,new TexCoord2f(1.0f,0.0f));
			sq.setTextureCoordinate(0, 2,new TexCoord2f(1.0f,1.0f));
			sq.setTextureCoordinate(0, 3,new TexCoord2f(0.0f,1.0f));


			
			BufferedImage img = _view2d.getBGImage(_view2d.get_viewmodel(), _slices.getActiveImageID());
			ImageComponent2D i2d = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA, img);
			Texture2D tex = new Texture2D(Texture2D.BASE_LEVEL,Texture2D.RGBA,img.getWidth(),img.getHeight());
			tex.setImage(0, i2d);
			
			Appearance ap_plane = new Appearance();
			
			ColoringAttributes color_ca = new ColoringAttributes(1, 1, 0, ColoringAttributes.FASTEST);
			color_ca.setColor(new Color3f(160f/255,32f/255,240f/255)); //   
			ap_plane.setColoringAttributes(color_ca);
			TransparencyAttributes trana = new TransparencyAttributes(TransparencyAttributes.FASTEST,0.3f);
			ap_plane.setTransparencyAttributes(trana);
			
			PolygonAttributes pa = new PolygonAttributes();
//			pa.setCullFace(PolygonAttributes.CULL_NONE);
			TextureAttributes texa = new TextureAttributes();
			
			texa.setTextureMode(TextureAttributes.COMBINE_ADD); //REPLACE 
			texa.setTextureBlendColor(new Color4f(1.0f,1.0f,1.0f,0.0f));
			ap_plane.setTextureAttributes(texa);
			pa.setPolygonMode(PolygonAttributes.POLYGON_FILL);
//			pa.setCullFace(PolygonAttributes.CULL_NONE);
			
			ap_plane.setPolygonAttributes(pa);
			ap_plane.setTexture(tex);
			
			Shape3D shape = new Shape3D(sq,ap_plane);
			
			
			return shape;
		}
		
		return new Shape3D();
	}

	private Shape3D cubeShape(int size) {
		Point3d p0 = new Point3d(-1*size, -1*size, 1*size);
		Point3d p1 = new Point3d(-1*size, 1*size, 1*size);
				
		Point3d p2 = new Point3d(1*size, -1*size, 1*size);
		Point3d p3 = new Point3d(1*size, 1*size, 1*size);
		
		Point3d p4 = new Point3d(-1*size, -1*size, -1*size);
		Point3d p5 = new Point3d(-1*size, 1*size, -1*size);
				
		Point3d p6 = new Point3d(1*size, -1*size, -1*size);
		Point3d p7 = new Point3d(1*size, 1*size, -1*size);
		
		Point3d p00 = new Point3d(-1*size, 1*size, 1*size);
		Point3d p10 = new Point3d(1*size, 1*size, 1*size);
				
		Point3d p20 = new Point3d(-1*size, -1*size, 1*size);
		Point3d p30 = new Point3d(1*size, -1*size, 1*size);
		
		Point3d p40 = new Point3d(-1*size, -1*size, -1*size);
		Point3d p50 = new Point3d(1*size, -1*size, -1*size);
				
		Point3d p60 = new Point3d(-1*size, 1*size, -1*size);
		Point3d p70 = new Point3d(1*size, 1*size, -1*size);
		
		Point3d p01 = new Point3d(-1*size, -1*size, -1*size);
		Point3d p11 = new Point3d(-1*size, -1*size, 1*size);
				
		Point3d p21 = new Point3d(1*size, 1*size, -1*size);
		Point3d p31 = new Point3d(1*size, 1*size, 1*size);
		
		Point3d p41 = new Point3d(-1*size, 1*size, -1*size);
		Point3d p51 = new Point3d(-1*size, 1*size, 1*size);
				
		Point3d p61 = new Point3d(1*size, -1*size, -1*size);
		Point3d p71 = new Point3d(1*size, -1*size, 1*size);
		
		
		LineArray la = new LineArray(24, LineArray.COORDINATES);
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
		
		
		LineAttributes linea = new LineAttributes(); 
		linea.setLineWidth(1.0f);
		linea.setLineAntialiasingEnable(true);
		
		Appearance ap = new Appearance();
		ColoringAttributes color_ca = new ColoringAttributes(1, 1, 0, ColoringAttributes.FASTEST);
		ap.setColoringAttributes(color_ca);
		ap.setLineAttributes(linea);
		TransparencyAttributes trana = new TransparencyAttributes(TransparencyAttributes.FASTEST,0.6f);
		ap.setTransparencyAttributes(trana);

		Shape3D line = new Shape3D(la,ap);
		return line;
	}
	
	private Shape3D createCoordinate(char coor,int size) {
		
		Point3d p02 = new Point3d(-1*size, 0, 0);
		Point3d p12 = new Point3d(1*size, 0, 0);
		Point3d p22 = new Point3d(0, -1*size, 0);
		Point3d p32 = new Point3d(0, 1*size, 0);
		Point3d p42 = new Point3d(0, 0, -1*size);
		Point3d p52 = new Point3d(0, 0, 1*size);
		Point3d p0 = new Point3d();
		Point3d p1 = new Point3d();
		
		Appearance ap = new Appearance();
		ColoringAttributes color_ca = new ColoringAttributes();

		switch (coor) {
		case 'x':
			p0 = p02;
			p1 = p12;
			color_ca = new ColoringAttributes(1, 0, 0, ColoringAttributes.FASTEST);
			break;
		case 'y':
			p0 = p22;
			p1 = p32;
			color_ca = new ColoringAttributes(0, 1, 0, ColoringAttributes.FASTEST);
			break;
		case 'z':
			p0 = p42;
			p1 = p52;
			color_ca = new ColoringAttributes(0, 0, 1, ColoringAttributes.FASTEST);
			break;
		default:
			break;
		}
		LineArray la = new LineArray(2, LineArray.COORDINATES);
		la.setCoordinate(0, p0);
		la.setCoordinate(1, p1);
		
		LineAttributes linea = new LineAttributes(); 
		linea.setLineWidth(1.0f);
		linea.setLineAntialiasingEnable(true);
		
		ap.setColoringAttributes(color_ca);
		ap.setLineAttributes(linea);
		TransparencyAttributes trana = new TransparencyAttributes(TransparencyAttributes.FASTEST,0.6f);
		ap.setTransparencyAttributes(trana);
		Shape3D shape = new Shape3D(la,ap);	
		return shape;
	}
	private Shape3D createChar(char coor,int scale) {
		Point3f p1 = new Point3f(1*scale, 0, 0);
		Point3f p2 = new Point3f(0, 1*scale, 0);
		Point3f p3 = new Point3f(0, 0, 1*scale);
		Font font = new Font("TimesRoman",Font.PLAIN,1+(int)(scale/6f));
		Font3D font3D = new Font3D(font,new FontExtrusion());
		Text3D text3D = new Text3D();
		switch (coor) {
		case 'x':
			text3D = new Text3D(font3D, "X", p1);
			break;
		case 'y':
			text3D = new Text3D(font3D, "Y", p2);
			break;
		case 'z':
			text3D = new Text3D(font3D, "Z", p3);
			break;
		default:
			break;
		}

		Appearance ap = new Appearance();
		ColoringAttributes color_ca = new ColoringAttributes(1, 1, 1, ColoringAttributes.FASTEST);
		ap.setColoringAttributes(color_ca);
		TransparencyAttributes trana = new TransparencyAttributes(TransparencyAttributes.FASTEST,0.8f);
		ap.setTransparencyAttributes(trana);
		Shape3D shape3d = new Shape3D(text3D, ap);
		return shape3d;
	}	


	/**
	 * this function is suitable for 3x3 matrix
	 * @param ma1
	 * @param ma2
	 * @return float[][]
	 */
	private float[][] matrixMulti(float[][] ma1,float[][] ma2){
		float[][] result = new float[3][3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				float temp = 0.0f;
				for (int x = 0; x < 3; x++) {
					temp += ma1[i][x] * ma2[x][j];
				}
				result[i][j] = temp;
			}
		}
		return result;
	}

	private Matrix3f calculateFinalRotationMatrix(float xaxis,float yaxis,float zaxis){
		float[][] ma = { { 1f, 0f, 0f }, 
						 { 0f, 0f, -1f }, 
						 { 0f, 1f, 0f }, };
		float[][] Rx = { { 1f, 0f, 0f }, 
						 { 0f, (float) Math.cos(xaxis), (float) Math.sin(xaxis) },
						 { 0f, -((float)Math.sin(xaxis)), (float) Math.cos(xaxis) } };
		
		float[][] Ry = { { (float) Math.cos(yaxis), 0f, -((float) Math.sin(yaxis)) }, 
						 { 0f, 1f, 0f },
						 { (float) Math.sin(yaxis), 0f, (float) Math.cos(yaxis) } };
		
		float[][] Rz = { { (float) Math.cos(zaxis), (float) Math.sin(zaxis), 0f },
						 { -((float)Math.sin(zaxis)), (float) Math.cos(zaxis), 0f }, 
						 { 0f, 0f, 1f } };
		float[][] temp = new float[3][3];
		
		temp = matrixMulti(Rx,ma);
		temp = matrixMulti(Ry,temp);
		temp = matrixMulti(Rz,temp);

		Matrix3f matrix3f = new Matrix3f();
		for (int i = 0; i < 3; i++)
			matrix3f.setRow(i, temp[i]);
		return matrix3f;
	}
 	public boolean toggleSeg(Segment seg) {
		String name = seg.getName();		
		if (_map_name_to_seg.get(name)!=null) {
			_map_name_to_seg.remove(name);
			_seg_name = null;
			if(_view_model==MARCHING_CUBE)
				_marching_cube_shape = createText("no segment currently",_scale);
			System.out.println("toggleSeg: remove");
		} else {			
				_map_name_to_seg.put(name, seg);
				_seg_name = seg.getName();
				if(_view_model==MARCHING_CUBE)
					_marching_cube_shape = segTrias(_map_name_to_seg.get(name),_size_cube);
				System.out.println("toggleSeg: add "+_seg_name);											
		}
		boolean gotcha = _map_name_to_seg.containsKey(name);
		update_view();
		return gotcha;
	}
 	
	public void setViewMode(int mode) {
		// you should do something with the new viewmode here
		switch(mode) {
		case 0:{
			_view_model = DEFAULT_CASE;
		}break;	
		case 1:{
			_view_model = TEXTURE_2D;
		}break;			
		case 2:{
			_view_model = POINT_CLOUD;
		}break;			 
		case 3:{
			_view_model = MARCHING_CUBE;

		}break;			
		default:
			break;
		}
		update_view();
	}


}
