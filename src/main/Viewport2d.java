package main;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import misc.BitMask;
import misc.DiFile;

/**
 * Two dimensional viewport for viewing the DICOM images + segmentations.
 * 
 * @author  Karl-Ingo Friese
 */
@SuppressWarnings("serial")
public class Viewport2d extends Viewport implements Observer {
	// the background image needs a pixel array, an image object and a MemoryImageSource
	private BufferedImage _bg_img;

	// each segmentation image needs the same, those are stored in a hashtable
	// and referenced by the segmentation name
	private Hashtable<String, BufferedImage> _map_seg_name_to_img;
	
	// this is the gui element where we actualy draw the images	
	private Panel2d _panel2d;
	
	// the gui element that lets us choose which image we want to show and
	// its data source (DefaultListModel)
	private ImageSelector _img_sel;
	private DefaultListModel<String> _slice_names;

	// width and heigth of our images. dont mix those with
	// Viewport2D width / height or Panel2d width / height!
	private int _w, _h;
	private int _display_X,_display_Y;
	/**
	 * add some new variable in aufgabe2
	 */
	private int _viewmodel;
	private final int TRANSVERSAL = 0;
	private final int SAGITTAL = 1;
	private final int FRONTAL = 2;
	//private final int NO_if_display_segment = 3;
	
	private String _seg_name;

	private int _window_width;
	private int _window_center;
	
	private boolean _if_display_RGseg;
	
	public DefaultListModel<String> get_slice_names(){
		return _slice_names;
	}
	public int get_viewmodel() {
		return _viewmodel;
	}
	/**
	 * Private class, implementing the GUI element for displaying the 2d data.
	 * Implements the MouseListener Interface.
	 */
	public class Panel2d extends JPanel implements MouseListener {
		public Panel2d() {
			super();
			setMinimumSize(new Dimension(DEF_WIDTH,DEF_HEIGHT));
			setMaximumSize(new Dimension(DEF_WIDTH,DEF_HEIGHT));
			setPreferredSize(new Dimension(DEF_WIDTH,DEF_HEIGHT));
			setBackground(Color.black);
			this.addMouseListener( this );
		}

		public void mouseClicked ( java.awt.event.MouseEvent e ) { 
			_display_X = e.getX();
			_display_Y = e.getY();
			if(_if_display_RGseg) {
				regionGrow();
			}	
			else {
				System.out.println("Panel2d::mouseClicked: x="+e.getX()+" y="+e.getY());
			}
			update_view();
		}
		public void mousePressed ( java.awt.event.MouseEvent e ) {}
		public void mouseReleased( java.awt.event.MouseEvent e ) {}
		public void mouseEntered ( java.awt.event.MouseEvent e ) {}
		public void mouseExited  ( java.awt.event.MouseEvent e ) {}
	
		/**
		 * paint should never be called directly but via the repaint() method.
		 */
		public void paint(Graphics g) {
			g.drawImage(_bg_img, 0, 0, this.getWidth(), this.getHeight(), this);
			//System.out.println("*****************"+this.getWidth());
			Enumeration<BufferedImage> segs = _map_seg_name_to_img.elements();	
			while (segs.hasMoreElements()) {
				g.drawImage(segs.nextElement(), 0, 0,  this.getWidth(), this.getHeight(), this);
			}
		}
	}
	
	/**
	 * Private class: The GUI element for selecting single DicomFiles in the View2D.
	 * Stores two references: the ImageStack (containing the DicomFiles)
	 * and the View2D which is used to show them.
	 * 
	 * @author kif
	 */
	private class ImageSelector extends JPanel {
		private JList<String> _jl_slices;
		private JScrollPane _jsp_scroll;
		
		/**
		 * Constructor with View2D and ImageStack reference.  
		 * The ImageSelector needs to know where to find the images and where to display them
		 */
		public ImageSelector() {
			_jl_slices = new JList<String>(_slice_names);
			//添加图片列表选择监听器
			_jl_slices.setSelectedIndex(0);
			_jl_slices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			_jl_slices.addListSelectionListener(new ListSelectionListener(){
				/**
				 * valueChanged is called when the list selection changes.   
				 */
			    public void valueChanged(ListSelectionEvent e) {
			      	int slice_index = _jl_slices.getSelectedIndex();
			      	 
			       	if (slice_index>=0){
			       		_slices.setActiveImage(slice_index);
			       	}
				 }
			});
			//列表滚动
			_jsp_scroll = new JScrollPane(_jl_slices);			
			_jsp_scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			_jsp_scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			
			setLayout(new BorderLayout());
			add(_jsp_scroll, BorderLayout.CENTER);
		}
	}
		
	/**
	 * Constructor, with a reference to the global image stack as argument.
	 * @param slices a reference to the global image stack
	 */
	public Viewport2d() {
		super();
		//_viewmodel = TRANSVERSAL;  // edition in exercise 2
		_slice_names = new DefaultListModel<String>();
		_slice_names.addElement(" ----- ");

		// create an empty 10x10 image as default
		_bg_img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		
		_map_seg_name_to_img = new Hashtable<String, BufferedImage>();

		// The image selector needs to know which images are to select
		_img_sel = new ImageSelector();

		setLayout( new BorderLayout() );
		_panel2d = new Panel2d();	//显示2d坐标位置数据	鼠标监听
        add(_panel2d, BorderLayout.CENTER );        
        add(_img_sel, BorderLayout.EAST );
		setPreferredSize(new Dimension(DEF_WIDTH+50,DEF_HEIGHT)); //设置首选尺寸
		
//		_min_slider = 50; //exercise 3
//		_max_slider = 50;
		_display_X = 0;
		_display_Y = 0;
		_if_display_RGseg = false;
		_window_width = 4096;
		_window_center = 1000;		
		_seg_name = new String();
	}


	/**
	 * This is private method is called when the current image width + height don't
	 * fit anymore (can happen after loading new DICOM series or switching viewmode).
	 * (see e.g. exercise 2)
	 */
	private void reallocate() {
		_w = _slices.getImageWidth();
		_h = _slices.getImageHeight();
		
		// create background image
		_bg_img = new BufferedImage(_w, _h, BufferedImage.TYPE_INT_ARGB);

		// create image for segment layers
		Enumeration<Segment> segs = _map_name_to_seg.elements();			
		while (segs.hasMoreElements()) {
			Segment seg = segs.nextElement();
			String name = seg.getName();
			BufferedImage seg_img = new BufferedImage(_w, _h, BufferedImage.TYPE_INT_ARGB);

			_map_seg_name_to_img.put(name, seg_img);
		}
	}
	
	/*
	 * Calculates the background image and segmentation layer images and forces a repaint.
	 * This function will be needed for several exercises after the first one.
	 * @see Viewport#update_view()
	 */
	public void update_view() {
		if (_slices.getNumberOfImages() == 0)
			return;
		// these are two variables you might need in exercise #2
		// int active_img_id = _slices.getActiveImageID();
		// DiFile active_file = _slices.getDiFile(active_img_id);
		
		// _w and _h need to be initialized BEFORE filling the image array !
		if (_bg_img==null || _bg_img.getWidth(null)!=_w || _bg_img.getHeight(null)!=_h) {
			reallocate();
		}
		
		// rendering the background picture
		if (_show_bg) { //父类里已经初始化为1
			// this is the place for the code displaying a single DICOM image
			// in the 2d viewport (exercise 2)
			//
			// the easiest way to set a pixel of an image is the setRGB method
			// example: _bg_img.setRGB(x,y, 0xff00ff00)
			//                                AARRGGBB
			// the resulting image will be used in the Panel2d::paint() method
			//active_file.getElement(0x00280004).getValueAsString()=="MONOCHROME2"						
			if(true) {
				//System.out.println(active_file.getElement(0x00280004).getValueAsString());
				switch(_viewmodel) {
				case TRANSVERSAL: 
					modusTransversal(); break;
				case SAGITTAL:
					modusSagittal(); break;
				case FRONTAL:
					modusFrontal(); break;
				default:break;		
				}
			}
						
		} else {
			// faster: access the data array directly (see below)
			final int[] bg_pixels = ((DataBufferInt) _bg_img.getRaster().getDataBuffer()).getData();
			for (int i=0; i<bg_pixels.length; i++) {
				bg_pixels[i] = 0xff000000;
			}
		}
		// rendering the segmentations. each segmentation is rendered in a
		// different image.
		for(String seg_name : _map_name_to_seg.keySet()) {		
			Segment seg = _map_name_to_seg.get(seg_name);
			//seg.create_range_seg(seg.getMaxSlider(), seg.getMinSlider(), _slices);
			int active_id = _slices.getActiveImageID();
			int[] pixel = dataProcess();
			BufferedImage buffer= new BufferedImage(_w, _h, BufferedImage.TYPE_INT_ARGB);
			
			switch(_viewmodel) {
			case 0:{
				for(int h=0;h<_h;h++) {
					for(int w=0;w<_w;w++) {
						if(seg.getMask(active_id).get(w,h))
							buffer.setRGB(w,h,pixel[h*_w+w]&(0xff000000+seg.getColor()));
						else
							buffer.setRGB(w,h,0xff000000);
					}
				}
			}	break;
			case 1:{
				int width = seg.getMask(0).get_h();
				int high = seg.getMaskNum();
				
				for(int layer=0;layer<high;layer++) {
					BitMask mask = seg.getMask(layer);
					for(int i=0;i<width;i++) {
						if(mask.get(active_id,i))
							buffer.setRGB(i,layer,pixel[layer*width+i]&(0xff000000+seg.getColor()));
						else
							buffer.setRGB(i,layer,0xff000000);
					}
				}
			}	break;
			case 2:{
				int width = seg.getMask(0).get_w();
				int high = seg.getMaskNum();
				for(int layer=0;layer<high;layer++) {
					BitMask mask = seg.getMask(layer);
					for(int i=0;i<width;i++) {
						if(mask.get(i,active_id))
							buffer.setRGB(i,layer,pixel[layer*width+i]&(0xff000000+seg.getColor()));
						else
							buffer.setRGB(i,layer,0xff000000);
					}					
				}
			}	break;
			default: break;
			}			
			_bg_img = buffer;
			_map_seg_name_to_img.put(seg_name, buffer);
		}
		
		
		repaint();
	}
	

	/**
	 * Implements the observer function update. Updates can be triggered by the global
	 * image stack.
	 */
	public void update(final Observable o, final Object obj ) {
		if (!EventQueue.isDispatchThread()) {
			// all swing thingies must be done in the AWT-EventQueue 
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					update(o,obj);
				}
			});
			return;
		}

		// boolean update_needed = false;
		Message m = (Message)obj;
		
		if (m._type == Message.M_CLEAR) {
			// clear all slice info
			_slice_names.clear();
			//_map_name_to_seg.clear();
			_map_seg_name_to_img.clear();

		}
		
		if (m._type == Message.M_NEW_IMAGE_LOADED) {
			// a new image was loaded and needs an entry in the ImageSelector's
			// DefaultListModel _slice_names
			String name = new String();
			int num = _slice_names.getSize();				
	    	name = ""+num;
			if (num<10) name = " "+name;				
			if (num<100) name = " "+name;		
			_slice_names.addElement(name);
			
			if (num==0) {
				// if the new image was the first image in the stack, make it active
				// (display it).
				reallocate();	
				//if(_viewmodel==TRANSVERSAL)
					_slices.setActiveImage(0);				
			}			
		}
		
		if (m._type == Message.M_NEW_ACTIVE_IMAGE) {
			update_view();			
		}
		
		if (m._type == Message.M_SEG_CHANGED) {
			Segment seg = (Segment)m._obj;
			String seg_name = seg.getName();
			boolean update_needed = _map_name_to_seg.containsKey(seg_name);
			if (update_needed) {
				_seg_name = seg_name;
				_map_name_to_seg.get(_seg_name).setMaxSlider(seg.getMaxSlider());
				_map_name_to_seg.get(_seg_name).setMinSlider(seg.getMinSlider());
				update_view();
			}
		}
		
//		if (m._type == Message.M_SEG_SLIDER) {
//			Segment seg = (Segment)m._obj;
//			_seg_name = seg.getName();
//			boolean update_needed = _map_name_to_seg.containsKey(_seg_name);
//			if (update_needed) {
//				_map_name_to_seg.get(_seg_name).setMaxSlider(seg.getMaxSlider());
//				_map_name_to_seg.get(_seg_name).setMinSlider(seg.getMinSlider());
//				update_view();
//			}
//		}
		if (m._type == Message.M_NEW_SETTING) {
			int[] value = (int[])m._obj;
			_window_width = value[0];
			_window_center = value[1];
			//System.out.println("_window_width: "+_window_width+"  _window_center: "+_window_center);
			update_view();
		}
		
	  }	
	
    /**
	 * Returns the current file.
	 * 
	 * @return the currently displayed dicom file
	 */
	public DiFile currentFile() {
		return _slices.getDiFile(_slices.getActiveImageID());
	}

	/**
	 * Toggles if a segmentation is shown or not.
	 */
	public boolean toggleSeg(Segment seg) {
		String name = seg.getName();
		boolean gotcha = _map_name_to_seg.containsKey(name);
		
		if (!gotcha) {
			// if a segmentation is shown, we need to allocate memory for pixels
			BufferedImage seg_img = new BufferedImage(_w, _h, BufferedImage.TYPE_INT_ARGB);
			_map_seg_name_to_img.put(name, seg_img);
		} else {
			_map_seg_name_to_img.remove(name);
		}
		// most of the buerocracy is done by the parent viewport class
		super.toggleSeg(seg);
		return gotcha;
	}
	
	/**
	 * Sets the view mode (transversal, sagittal, frontal).
	 * This method will be implemented in exercise 2.
	 * 
	 * @param mode the new viewmode
	 */
	public void setViewMode(int mode) {
		// you should do something with the new viewmode here
		switch(mode) {
		case 0:{
			_viewmodel = TRANSVERSAL;
		}break;			
		case 1:{
			_viewmodel = SAGITTAL;
		}break;			 
		case 2:{
			_viewmodel = FRONTAL;
		}break;			
		default:break;
		}
		_slices.initThreeViewModel(_viewmodel);
		update_view();
	}
	
	public void selectRegionGrowOrNot() {
		
		_if_display_RGseg = !_if_display_RGseg;
	}
	private int[] dataProcess() {
		int active_img_id = _slices.getActiveImageID();
		int max = _window_width;
		int window_center = _window_center;
		Integer[][] prime_pixel = new Integer[_h][_w];
		
		switch (_viewmodel) {		
		case TRANSVERSAL:{
			prime_pixel = _slices.get_volum_pixel_data(active_img_id);
		}	break;		
		case SAGITTAL:{
			prime_pixel = _slices.get_sagittal_img(active_img_id);
		}	break;
		case FRONTAL:{
			prime_pixel = _slices.get_frontal_img(active_img_id);
		}	break;
		default:
			break;
		}
						
		int[] scale_pixel = new int[_w*_h];
		int[] pixel = new int[scale_pixel.length];
		for(int i=0;i<_h;i++) {
			for(int j=0;j<_w;j++) {
				if(prime_pixel[i][j]<=(window_center -0.5 - (max-1)/2)) {
					scale_pixel[i*_w+j] = 0;
				}
				else if(prime_pixel[i][j] > (window_center -0.5 + (max-1)/2)) {
					scale_pixel[i*_w+j] = 255;
				}
				else {
					scale_pixel[i*_w+j] = (int)(((prime_pixel[i][j]-(window_center-0.5))/(max-1)+0.5)*(255-0)+0);
				}
				pixel[i*_w+j] = (0xff<<24) + (scale_pixel[i*_w+j]<<16) + (scale_pixel[i*_w+j]<<8) + scale_pixel[i*_w+j];
			}
		}
		return pixel;
	}
	/**
	 * set different model
	 * @author xiao; Tang
	 */
	public void modusTransversal() {
		System.out.println("Viewmode "+"Transversal");
		
		int active_img_id = _slices.getActiveImageID();
		DiFile active_file = _slices.getDiFile(active_img_id);
			
		_w = active_file.getImageWidth();
		_h = active_file.getImageHeight();
		_bg_img = new BufferedImage(_w, _h, BufferedImage.TYPE_INT_ARGB);
		
		int[] pixel = dataProcess();
		if(_if_display_RGseg) {
			for(int h=0;h<_h;h++) {
				for(int w=0;w<_w;w++) {
					if(_slices._seg_RegionGrow.getMask(active_img_id).get(w,h))
						_bg_img.setRGB(w,h,pixel[h*_w+w]&(0xff000000+0xffff));
					else
						_bg_img.setRGB(w,h,0xff000000);
				}
			}
		}else {
			for (int i=0; i<pixel.length; i++) {
				_bg_img.setRGB(i%_w,i/_w,pixel[i]);
			}
		}
		
			
			
		/*--------------------------------------------------------------
		final int[] bg_pixels = ((DataBufferInt) _bg_img.getRaster().getDataBuffer()).getData();
		for (int i=0; i<bg_pixels.length; i++) {
			bg_pixels[i] = pixel[i];
		}*/
		
		
	}
	
	public void modusSagittal() {
		System.out.println("Viewmode "+"Sagittal");

		DiFile first_file = _slices.getDiFile(0);
		int active_id = _slices.getActiveImageID();
		_w = first_file.getImageHeight();
		_h = _slices.getNumberOfImages();
		_bg_img = new BufferedImage(_w, _h, BufferedImage.TYPE_INT_ARGB);	
		int[] pixel = dataProcess();
		if(_if_display_RGseg) { 
			int width = _slices._seg_RegionGrow.getMask(0).get_h();
			int high = _slices._seg_RegionGrow.getMaskNum();
			for(int layer=0;layer<high;layer++) {
				BitMask mask = _slices._seg_RegionGrow.getMask(layer);
				for(int i=0;i<width;i++) {
					if(mask.get(active_id,i))
						_bg_img.setRGB(i,layer,pixel[layer*width+i]&(0xff000000+0xffff));
					else
						_bg_img.setRGB(i,layer,0xff000000);
				}
				
			}
		}else {
			final int[] bg_pixels = ((DataBufferInt) _bg_img.getRaster().getDataBuffer()).getData();
			for (int i=0; i<bg_pixels.length; i++) {
				bg_pixels[i] = pixel[i];
				//bg_pixels[i] = 0xAA000000;
			}
		}	
	}
	
	public void modusFrontal() {
		System.out.println("Viewmode "+"Frontal");
		
		DiFile first_file = _slices.getDiFile(0);
		int active_id = _slices.getActiveImageID();
		_w = first_file.getImageWidth();
		_h = _slices.getNumberOfImages();
		_bg_img = new BufferedImage(_w, _h, BufferedImage.TYPE_INT_ARGB);
		int[] pixel = dataProcess();
		if(_if_display_RGseg) {
			int width = _slices._seg_RegionGrow.getMask(0).get_w();
			int high = _slices._seg_RegionGrow.getMaskNum();
			for(int layer=0;layer<high;layer++) {
				BitMask mask = _slices._seg_RegionGrow.getMask(layer);
				for(int i=0;i<width;i++) {
					if(mask.get(i,active_id))
						_bg_img.setRGB(i,layer,pixel[layer*width+i]&(0xff000000+0xffff));
					else
						_bg_img.setRGB(i,layer,0xff000000);
				}				
			}
		}else {
			final int[] bg_pixels = ((DataBufferInt) _bg_img.getRaster().getDataBuffer()).getData();
			for (int i=0; i<bg_pixels.length; i++) {
				bg_pixels[i] = pixel[i];
			}
		}		
	}
	
	/**
	 *  the region grow algorithms, exercise 4.2
	 *  
	 */
	public void regionGrow() {
		int x = (int)Math.rint(((double)_display_X*_w)/super.getWidth());//get nearest Integer
		int y = (int)Math.rint(((double)_display_Y*_h)/super.getHeight());
//		int x = (int)Math.rint(((double)_display_X)/500*_w);//get nearest Integer
//		int y = (int)Math.rint(((double)_display_Y)/500*_h);
		int active_img_id = _slices.getActiveImageID();
		int grauwert = 0;
		switch (_viewmodel) {		
		case TRANSVERSAL:{
			grauwert = _slices.get_volum_pixel_data(active_img_id)[y][x];
		}	break;		
		case SAGITTAL:{
			grauwert = _slices.get_sagittal_img(active_img_id)[y][x];
		}	break;
		case FRONTAL:{
			grauwert = _slices.get_frontal_img(active_img_id)[y][x];
		}	break;
		default:
			break;
		}
		
		int v = (int)((double)grauwert/10);
		int intension_min = 0;
		int intension_max = 0;
		int percent = 2; //20%
		if(grauwert>0) {
			intension_min = grauwert - percent*v;
			intension_max = grauwert + percent*v;
		}
		else {
			intension_min = grauwert + percent*v;
			intension_max = grauwert - percent*v;
		}
		System.out.println("seed grauwert: "+grauwert+" intension_min: "+intension_min+" intension_max: "+intension_max);
		
		int num_of_img = _slices.getNumberOfImages();
		int size_flat = _slices.get_volum_pixel_data(0).length;
		//---------------------------------------------
		
		BitMask[][] layer = new BitMask[2][num_of_img];
		for(int i=0;i<2;i++)
		for (int j=0; j<num_of_img; j++) {
			layer[i][j] = new BitMask(size_flat,size_flat);
		}
		switch (_viewmodel) {		
		case TRANSVERSAL:{
			Algorithmus3d(layer,x,y,active_img_id,intension_min,intension_max);	//x y z 空间中的点坐标
			}break;		
		case SAGITTAL:{
			Algorithmus3d(layer,active_img_id,x,y,intension_min,intension_max);
		}	break;
		case FRONTAL:{
			Algorithmus3d(layer,x,active_img_id,y,intension_min,intension_max);
		}	break;
		default:
			break;
		}
		_slices._seg_RegionGrow.setBitmask(layer[0]);
		_slices._seg_RegionGrow.setName("RegionGrow");
//		System.out.println(layer[0][active_img_id].toString());
		//recursiveAlgorithmus3d(layer,x,y,active_img_id,intension_min,intension_max);
		//---------------------------------------------
		/*
		BitMask[] layer = new BitMask[2];
		for(int i=0;i<2;i++)
			layer[i] = new BitMask(size_flat,size_flat);
		recursiveAlgorithmus2d(layer,x,y,active_img_id,intension_min,intension_max);
		*/
	}
	private void recursiveAlgorithmus2d(BitMask[] layer,int x,int y,int active,int i_min,int i_max) {
		layer[1].set(x, y, true);
		int size_flat = _slices.get_volum_pixel_data(0).length;
		int grauwert = _slices.get_volum_pixel_data(active)[y][x];
		if(grauwert>=i_min && grauwert<=i_max) {
			layer[0].set(x, y, true);
			if((x-1 >= -1) && (!layer[1].get(x-1, y))) {
				recursiveAlgorithmus2d(layer,x-1,y,active,i_min,i_max);
			}
			if((x+1 <= size_flat) && (!layer[1].get(x+1, y))) {
				recursiveAlgorithmus2d(layer,x-1,y,active,i_min,i_max);
			}
			if((y-1 >= -1) && (!layer[1].get(x, y-1))) {
				recursiveAlgorithmus2d(layer,x,y-1,active,i_min,i_max);
			}
			if((y+1 <= size_flat) && (!layer[1].get(x, y+1))) {
				recursiveAlgorithmus2d(layer,x,y+1,active,i_min,i_max);
			}
		}
		else {
			layer[0].set(x, y, false);
			return;
		}
	}
	private void recursiveAlgorithmus3d(BitMask[][] layer,int x,int y,int layer_num, int i_min,int i_max) { //相对于_volum_pixel_data中 x y的位置
		if(layer[1][layer_num].get(x, y))
			return;
		layer[1][layer_num].set(x, y, true);
		int size_flat = _slices.get_volum_pixel_data(0).length;
		int size_vertical = _slices.getNumberOfImages();
		int grauwert = _slices.get_volum_pixel_data(layer_num)[y][x];
		if((grauwert>0&&grauwert>=i_min && grauwert<=i_max )			
				//||(grauwert<0&&(grauwert<=i_min && grauwert>=i_max))
				) {
			layer[0][layer_num].set(x, y, true);
			if((x-1 >= -1) && (!layer[1][layer_num].get(x-1, y))) {
				recursiveAlgorithmus3d(layer,x-1,y,layer_num,i_min,i_max);
			}
			if((x+1 <= size_flat) && (!layer[1][layer_num].get(x+1, y))) {
				recursiveAlgorithmus3d(layer,x+1,y,layer_num,i_min,i_max);
			}
			if((y-1 >= -1) && (!layer[1][layer_num].get(x, y-1))) {
				recursiveAlgorithmus3d(layer,x,y-1,layer_num,i_min,i_max);
			}
			if ((y + 1 <= size_flat) && (!layer[1][layer_num].get(x, y + 1))) {
				recursiveAlgorithmus3d(layer, x, y + 1, layer_num, i_min, i_max);
			}
//			if ((layer_num - 1 >= -1) && (!layer[1][layer_num - 1].get(x, y))) {
//				recursiveAlgorithmus3d(layer, x, y, layer_num - 1, i_min, i_max);
//			}
//			if ((layer_num + 1 <= size_vertical) && (!layer[1][layer_num + 1].get(x, y))) {
//				recursiveAlgorithmus(layer, x, y, layer_num + 1, i_min, i_max);
//			}						
		}
		else {
			layer[0][layer_num].set(x, y, false);
			return;
		}		
	}

	//BitMask[0][] for segment, BitMask[1][] flag for recursive state
	private void Algorithmus3d(BitMask[][] layer,int x,int y,int layer_num, int i_min,int i_max) {
		class Coordinate{
			private int cx,cy,cz;
			Coordinate(){
				cx = 0;
				cy = 0;
				cz = 0;
			}
			Coordinate(int x,int y, int z){
				cx = x;
				cy = y;
				cz = z;
			}
		};
		
		int size_flat = _slices.get_volum_pixel_data(0).length;
		int size_vertical = _slices.getNumberOfImages();
		Queue<Coordinate> will_be_researched = new LinkedBlockingQueue<Coordinate>();				
		will_be_researched.add(new Coordinate(x,y,layer_num));
		layer[1][layer_num].set(x,y,true);
		Coordinate current = new Coordinate();
		do {
			current = will_be_researched.remove();
			
			int x_ = current.cx;
			int y_ = current.cy;
			int z_ = current.cz;			
			int grauwert = _slices.get_volum_pixel_data(z_)[y_][x_];
			if((grauwert>=i_min && grauwert<=i_max )){
				layer[0][z_].set(x_, y_, true);
				if((x_ > 0)&&(!layer[1][z_].get(x_-1, y_))) {
					will_be_researched.add(new Coordinate(x_-1, y_, z_));
					layer[1][z_].set(x_-1,y_,true);
				}
				if((x_ < size_flat-1)&&(!layer[1][z_].get(x_+1, y_))) {
					will_be_researched.add(new Coordinate(x_+1, y_, z_));
					layer[1][z_].set(x_+1,y_,true);
				}
				if((y_ > 0)&&(!layer[1][z_].get(x_, y_-1))) {
					will_be_researched.add(new Coordinate(x_, y_-1, z_));
					layer[1][z_].set(x_,y_-1,true);
				}
				if((y_ < size_flat-1)&&(!layer[1][z_].get(x_, y_+1))) {
					will_be_researched.add(new Coordinate(x_, y_+1, z_));
					layer[1][z_].set(x_,y_+1,true);
				}
				if((z_ > 0)&&(!layer[1][z_-1].get(x_, y_))) {
					will_be_researched.add(new Coordinate(x_, y_, z_-1));
					layer[1][z_-1].set(x_,y_,true);
				}
				if((z_ < size_vertical-1)&&(!layer[1][z_+1].get(x_, y_))) {
					will_be_researched.add(new Coordinate(x_, y_, z_+1));
					layer[1][z_+1].set(x_,y_,true);
				}
			}
			else {
				layer[0][z_].set(x_, y_, false);
			}		
		//System.out.println("x: "+current.cx+" y: "+current.cy+" z: "+current.cz );
		}while(!will_be_researched.isEmpty());
		
	}
	
	public BufferedImage getBGImage(int model,int pos){		
		return _bg_img;
	}
}
