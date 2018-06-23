package main;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * GUI for setting window_width and window_center
 * @author xiao tang
 *
 */
@SuppressWarnings("serial")
public class ToolWindowSelector extends JPanel{
	private int _width, _center, _scale3d;
	private JSlider _width_slider, _center_slider,_scale_slider;
	private JLabel _range_sel_title,_range_sel_title3d, _width_label, _center_label,_scale3d_label;
//	private JFrame _frame;
	public ToolWindowSelector() {
		final ImageStack slices = ImageStack.getInstance();	
		       
		_range_sel_title = new JLabel("setting window width and center(for 2d):   min  ->  max");
		_range_sel_title3d = new JLabel("setting scale (for 3d):   max  ->  min");
		int range_max = 4095;
		_width = 4095;
		_center = 1000;
		_scale3d = 128;
		
		_width_label = new JLabel("Width:");
		_center_label = new JLabel("Center:");
		_scale3d_label = new JLabel("Scale:");
		
		_width_slider = new JSlider(0, range_max, _width);
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

		_scale_slider = new JSlider(64, 256, _scale3d);
		_scale_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					_scale3d = (int)source.getValue();					
					int value = _scale3d;
					//slices.setting3DChanged(value);
				}
			}
		});

		
		
		_center_slider = new JSlider(0, range_max, _center);
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
		
//		JPanel panel = new JPanel();
		this.setLayout(null);
		_range_sel_title.setBounds(20,20,600,50);
		_range_sel_title3d.setBounds(20,140,600,50);
		_width_label.setBounds(20,60,80,50);
		_center_label.setBounds(20,100,80,50);
		_scale3d_label.setBounds(20,180,80,50);
		_width_slider.setBounds(90, 60, 600, 50);
		_center_slider.setBounds(90, 100, 600, 50);		
		_scale_slider.setBounds(90, 180, 600, 50);
		this.add(_range_sel_title);
		this.add(_width_label);
		this.add(_center_label);
		this.add(_width_slider);
		this.add(_center_slider);
		this.add(_range_sel_title3d);
		this.add(_scale_slider);
		this.add(_scale3d_label);
		//_frame.add(panel);
		//_frame.setVisible(true);
	}
}
