package main;

import javax.swing.JFrame;
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
	private int _width, _center;
	private JSlider _width_slider, _center_slider;
	private JLabel _range_sel_title, _width_label, _center_label;
	private JFrame _frame;
	public ToolWindowSelector() {
		final ImageStack slices = ImageStack.getInstance();	
		
		_frame = new JFrame("setting");
		_frame.setSize(800, 200);
        _frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
		_range_sel_title = new JLabel("setting window width and center.      range: 0-4095");
		
		int range_max = 4095;
		_width = 4095;
		_center = 2047;
		
		_width_label = new JLabel("Width:");
		_center_label = new JLabel("Center:");
		
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
		
		JPanel panel = new JPanel();
		panel.setLayout(null);
		_range_sel_title.setBounds(20,20,600,50);
		_width_label.setBounds(20,60,80,50);
		_center_label.setBounds(20,100,80,50);
		_width_slider.setBounds(90, 60, 600, 50);
		_center_slider.setBounds(90, 100, 600, 50);
		panel.add(_range_sel_title);
		panel.add(_width_label);
		panel.add(_center_label);
		panel.add(_width_slider);
		panel.add(_center_slider);
		_frame.add(panel);
		_frame.setVisible(true);
	}
}
