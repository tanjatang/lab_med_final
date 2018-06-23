package main;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * GUI for setting window_xaxis and window_yaxis
 * @author xiao tang
 *
 */
@SuppressWarnings("serial")
public class Tools3D extends JPanel{
	private int _xaxis, _yaxis, _zaxis, _scale3d;
	private JSlider _xaxis_slider, _yaxis_slider,_zaxis_slider,_scale_slider;
	private JLabel _range_sel_title,_range_sel_scale, _xaxis_label, _yaxis_label,_zaxis_label, _scale3d_label;
//	private JFrame _frame;
	public Tools3D() {
		final ImageStack slices = ImageStack.getInstance();	
		       
		_range_sel_title = new JLabel("setting rotation :   -π  ---->  π");
		_range_sel_scale = new JLabel("setting scale :   max  ---->  min");
//		_range_sel_title3d = new JLabel("setting scale (for 3d):   max  ->  min");
		int range_max = (int)(100*Math.PI);
		_xaxis = 0;
		_yaxis = 0;
		_zaxis = 0;
		_scale3d = 200;
		
		_xaxis_label = new JLabel("X:");
		_yaxis_label = new JLabel("Y:");
		_zaxis_label = new JLabel("Z:");
		_scale3d_label = new JLabel("Scale:");
		
		_xaxis_slider = new JSlider(-(range_max), range_max, _xaxis);
		_xaxis_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					_xaxis = (int)source.getValue();
					int[] value = new int[4];
					value[0] = _xaxis;
					value[1] = _yaxis;
					value[2] = _zaxis;
					value[3] = _scale3d;
					slices.setting3DChanged(value);
					//System.out.println("_xaxis_slider stateChanged: "+_xaxis);
				}
			}
		});
		
		_yaxis_slider = new JSlider(-(range_max), range_max, _yaxis);
		_yaxis_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					_yaxis = (int)source.getValue();
					int[] value = new int[4];
					value[0] = _xaxis;
					value[1] = _yaxis;
					value[2] = _zaxis;
					value[3] = _scale3d;
					slices.setting3DChanged(value);
					//System.out.println("_yaxis_slider stateChanged: "+_yaxis);
				}
			}
		});				
		_zaxis_slider = new JSlider(-(range_max), range_max, _zaxis);
		_zaxis_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					_zaxis = (int)source.getValue();
					int[] value = new int[4];
					value[0] = _xaxis;
					value[1] = _yaxis;
					value[2] = _zaxis;
					value[3] = _scale3d;
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
					int[] value = new int[4];
					value[0] = _xaxis;
					value[1] = _yaxis;
					value[2] = _zaxis;
					value[3] = _scale3d;
					slices.setting3DChanged(value);
					//System.out.println("_scale_slider stateChanged: "+_scale3d);

				}
			}
		});

		
		
		
		this.setLayout(null);
		_range_sel_title.setBounds(20,20,600,50);
		_range_sel_scale.setBounds(20,180,600,50);
		_xaxis_label.setBounds(20,60,80,50);
		_yaxis_label.setBounds(20,100,80,50);
		_zaxis_label.setBounds(20,140,80,50);
		_scale3d_label.setBounds(20,220,80,50);
		_xaxis_slider.setBounds(90, 60, 600, 50);
		_yaxis_slider.setBounds(90, 100, 600, 50);	
		_zaxis_slider.setBounds(90, 140, 600, 50);	
		_scale_slider.setBounds(90, 220, 600, 50);
		this.add(_range_sel_title);
		this.add(_range_sel_scale);
		this.add(_xaxis_label);
		this.add(_yaxis_label);
		this.add(_zaxis_label);
		this.add(_xaxis_slider);
		this.add(_yaxis_slider);
		this.add(_zaxis_slider);
		this.add(_scale_slider);
		this.add(_scale3d_label);
	}
}
