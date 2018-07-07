package main;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;

/**
 * GUI for setting window_width and window_center
 * @author xiao tang
 *
 */
@SuppressWarnings("serial")
public class ToolWindowSelector extends JPanel{
	private int _width, _center;
	private JSlider _width_slider, _center_slider;
	private JLabel _range_sel_title_2d, _width_label, _center_label;
	
	private int _xaxis, _yaxis, _zaxis, _scale3d;
	private JSlider _xaxis_slider, _yaxis_slider,_zaxis_slider,_scale_slider;
	private JLabel _range_sel_title_3d,_range_sel_scale, _xaxis_label, _yaxis_label,_zaxis_label, _scale3d_label;

	private int _size_cube;
	private JLabel _size_marching_cube;
	private JFormattedTextField _text;
//	private JFrame _frame;
	public ToolWindowSelector() {
		final ImageStack slices = ImageStack.getInstance();	
		       
		_range_sel_title_2d = new JLabel("setting window width and center(for 2d):   min  ---->  max");
		_range_sel_title_3d = new JLabel("setting rotation (for 3d):   -π  ---->  π");
		_range_sel_scale = new JLabel("setting scale (for 3d):   max  ---->  min");
		int range_max_2d = 4095;
		_width = 4095;
		_center = 1000;
		
		int range_max_3d = (int)(100*Math.PI);
		_xaxis = 0;
		_yaxis = 0;
		_zaxis = 0;
		_scale3d = 200;

		
		_width_label = new JLabel("Width:");
		_center_label = new JLabel("Center:");
		
		_xaxis_label = new JLabel("X:");
		_yaxis_label = new JLabel("Y:");
		_zaxis_label = new JLabel("Z:");
		_scale3d_label = new JLabel("Scale:");

		_size_cube = 1;
		_size_marching_cube = new JLabel("cube size:");
		
		_text = new JFormattedTextField(new java.text.DecimalFormat("#0"));
		_text.setPreferredSize(new java.awt.Dimension(50, 21));
		_text.addKeyListener(new java.awt.event.KeyAdapter() {
			 public void keyReleased(java.awt.event.KeyEvent evt) {	  

				 String old = _text.getText();
				 if(!old.equals("")) {
					 int x=Integer.parseInt(old);	
					 if(x>=1&&x<=4) {
						 _size_cube = x;
						 int[] value = new int[5];
							value[0] = _xaxis;
							value[1] = _yaxis;
							value[2] = _zaxis;
							value[3] = _scale3d;
							value[4] = _size_cube;
							slices.setting3DChanged(value);
					 }
						 
				 }
	           }
	       });
		
		_width_slider = new JSlider(0, range_max_2d, _width);
		_width_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					_width = (int)source.getValue();
					int[] value = new int[2];
					value[0] = _width;
					value[1] = _center;
					slices.widthCenterChanged(value);
					System.out.println("_width_slider stateChanged: "+_width);
				}
			}
		});



		
		
		_center_slider = new JSlider(0, range_max_2d, _center);
		_center_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					_center = (int)source.getValue();
					int[] value = new int[2];
					value[0] = _width;
					value[1] = _center;
					slices.widthCenterChanged(value);
					System.out.println("_center_slider stateChanged: "+_center);
				}
			}
		});				
		
		
		
		_xaxis_slider = new JSlider(-(range_max_3d), range_max_3d, _xaxis);
		_xaxis_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					_xaxis = (int)source.getValue();
					int[] value = new int[5];
					value[0] = _xaxis;
					value[1] = _yaxis;
					value[2] = _zaxis;
					value[3] = _scale3d;
					value[4] = _size_cube;
					slices.setting3DChanged(value);
				}
			}
		});
		
		_yaxis_slider = new JSlider(-(range_max_3d), range_max_3d, _yaxis);
		_yaxis_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					_yaxis = (int)source.getValue();
					int[] value = new int[5];
					value[0] = _xaxis;
					value[1] = _yaxis;
					value[2] = _zaxis;
					value[3] = _scale3d;
					value[4] = _size_cube;
					slices.setting3DChanged(value);
					//System.out.println("_yaxis_slider stateChanged: "+_yaxis);
				}
			}
		});				
		_zaxis_slider = new JSlider(-(range_max_3d), range_max_3d, _zaxis);
		_zaxis_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					_zaxis = (int)source.getValue();
					int[] value = new int[5];
					value[0] = _xaxis;
					value[1] = _yaxis;
					value[2] = _zaxis;
					value[3] = _scale3d;
					value[4] = _size_cube;
					slices.setting3DChanged(value);
					//System.out.println("_yaxis_slider stateChanged: "+_zaxis);
				}
			}
		});				

		_scale_slider = new JSlider(100, 300, _scale3d);
		_scale_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					_scale3d = (int)source.getValue();	
					int[] value = new int[5];
					value[0] = _xaxis;
					value[1] = _yaxis;
					value[2] = _zaxis;
					value[3] = _scale3d;
					value[4] = _size_cube;
					slices.setting3DChanged(value);

				}
			}
		});

		
		this.setLayout(null);
		_range_sel_title_2d.setBounds(20,20,600,50);
		_width_label.setBounds(20,50,80,50);
		_width_slider.setBounds(90, 50, 600, 50);
		_center_label.setBounds(20,80,80,50);
		_center_slider.setBounds(90, 80, 600, 50);	
//------------------------------------------------------
		_range_sel_title_3d.setBounds(20,110,600,50);
		_xaxis_label.setBounds(20,140,80,50);
		_xaxis_slider.setBounds(90, 140, 600, 50);
		_yaxis_label.setBounds(20,170,80,50);
		_yaxis_slider.setBounds(90, 170, 600, 50);	
		_zaxis_label.setBounds(20, 200,80,50);
		_zaxis_slider.setBounds(90, 200, 600, 50);	
//------------------------------------------------------		
		_range_sel_scale.setBounds(20,230,600,50);
		_scale3d_label.setBounds(20,260,80,50);
		_scale_slider.setBounds(90, 260, 600, 50);
		_size_marching_cube.setBounds(20,290,80,50);
		_text.setBounds(90, 310, 30, 30);
		
		this.add(_range_sel_title_2d);
		this.add(_width_label);
		this.add(_center_label);
		this.add(_width_slider);
		this.add(_center_slider);
		this.add(_range_sel_title_3d);
		this.add(_scale_slider);
		this.add(_scale3d_label);
		
		this.add(_range_sel_scale);
		this.add(_xaxis_label);
		this.add(_yaxis_label);
		this.add(_zaxis_label);
		this.add(_xaxis_slider);
		this.add(_yaxis_slider);
		this.add(_zaxis_slider);
		this.add(_scale_slider);
		this.add(_scale3d_label);
		this.add(_size_marching_cube);
		this.add(_text);

		//_frame.add(panel);
		//_frame.setVisible(true);
	}
}
