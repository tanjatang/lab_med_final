package main;

import misc.BitMask;

/**
 * This class represents a segment. Simply spoken, a segment has a unique name,
 * a color for displaying in the 2d/3d viewport and contains n bitmasks where n is the
 * number of images in the image stack.
 * 
 * @author  Karl-Ingo Friese
 */
public class Segment {
	private String _name;		// the segment name
	private int _color;			// the segment color
	private int _w;				// Bitmask width				
	private int _h;				// Bitmask height
	private BitMask[] _layers;	// each segment contains an array of n bitmasks
	
	public BitMask[] get_bitMaskArray() {
		return _layers;
	}
	private int _max_slider,_min_slider;
	public void setMaxSlider(int max) {
		_max_slider = max;
	}
	public void setMinSlider(int min) {
		_min_slider = min;
	}
	public int getMaxSlider() {
		return _max_slider;
	}
	public int getMinSlider() {
		return _min_slider;
	}
	

	/**
	 * Constructor for new segment objects.
	 * 
	 * @param name			the name of the new segment
	 * @param w				the width of the bitmasks
	 * @param h				the height of the bitmasks
	 * @param layer_num		the total number of bitmasks
	 */
	public Segment(String name, int w, int h, int layer_num) {
		this._name = name;
		this._w = w;
		this._h = h;
		
		_color = 0xff00ff;		
		_layers = new BitMask[layer_num];
		
		for (int i=0; i<layer_num; i++) {
			_layers[i] = new BitMask(_w,_h);
		}
	}
	public Segment(Segment seg) {
		this._name = seg.getName();
		this._w = seg._w;
		this._h = seg._h;
		this._min_slider = seg.getMinSlider();
		this._max_slider = seg.getMaxSlider();
		this._layers = seg._layers;
	}
	/**
	 * Returns the number of bitmasks contained in this segment.
	 * 
	 * @return  the number of layers.
	 */
	public int getMaskNum() {
		return _layers.length;
	}

	/**
	 * Returns the Bitmask of a single layer.
	 * 
	 * @param i	the layer number
	 * @return	the coresponding bitmask
	 */
	public BitMask getMask(int i) {
		return _layers[i];
	}

	/**
	 * Returns the name of the segment.
	 * 
	 * @return  the segment name.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Sets the name of the segment.
	 * 
	 * @param name	the new segment name
	 */
	public void setName(String name) {
		_name = name;
	}

	/**
	 * Returns the segment color as the usual rgb int value.
	 * 
	 * @return the color
	 */
	public int getColor() {
		return _color;
	}

	/**
	 * Sets the segment color.
	 * 
	 * @param color the segment color (used when displaying in 2d/3d viewport)
	 */
	public void setColor(int color) {
		_color = color;
	}
	public void setBitmask(BitMask bit[]) {
		_layers = bit;
	}
	/**
	 * @author Xiao; Tang --exercise 3 
	 * @param max
	 * @param min
	 * @param slices
	 */
	public void create_range_seg(int max, int min,ImageStack slices) {
		int max_original = (1 << slices.getDiFile(0).getBitsStored())-1;
		int slope = slices.getDiFile(0).getSlope();
		int intercept = slices.getDiFile(0).getIntercept();
		int grenz_min = slope*((max_original/100)*min) + intercept;
		int grenz_max = slope*((max_original/100)*max + (max_original%100)) + intercept;
		int width = slices.getDiFile(0).getImageWidth();
		int high = slices.getDiFile(0).getImageHeight();
		Integer[][] prime_pixel = new Integer[high][width];	
		for(int layer=0;layer<_layers.length;layer++) {
			prime_pixel = slices.get_volum_pixel_data(layer);
			for(int h=0;h<high;h++) { //j:column,  i:row
				for(int w=0;w<width;w++) {
					if((prime_pixel[h][w]>=grenz_min) && (prime_pixel[h][w]<=grenz_max)) {
						_layers[layer].set(w, h, true);
					}
					else {
						_layers[layer].set(w, h, false);
					}
					//System.out.print(" "+prime_pixel[i][j]);
				}
				//System.out.println(" ");
			}				
			//System.out.println("++++++++++++++++++++++++++++++++++++++");
		}	
	}
}
