/**
 * Created on 2015. 4. 3.
 * @author cskim -- hufs.ac.kr, Dept of CSE
 * Copy Right -- Free for Educational Purpose
 */
package hufs.cse.grimpan.strategy;

import java.awt.Color;
import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * @author cskim
 *
 */
public class GrimPanController {
	
	private GrimPanModel model = null;
	private GrimPanFrameMain frameView = null;
	private DrawPanelView drawPanelView = null;

	public GrimPanController(GrimPanFrameMain f, GrimPanModel m, DrawPanelView p){
		
		createGrimPanFrameView(f, m, p);
	}
	
	private void createGrimPanFrameView(GrimPanFrameMain f, GrimPanModel m, DrawPanelView p){
		frameView = f;
		model = m;
		drawPanelView = p;
		
		frameView.setVisible(true);
		model.setController(this);
		
	}
	public void openAction() {
		if (frameView.jFileChooser1.showOpenDialog(frameView) ==
				JFileChooser.APPROVE_OPTION) {
			File selFile = frameView.jFileChooser1.getSelectedFile();
			readShapeFromSaveFile(selFile);
			model.setSaveFile(selFile);
			drawPanelView.repaint();
		}
	}
	public void saveAction() {
		if (model.getSaveFile()==null){
			model.setSaveFile(new File(model.getDefaultDir()+"noname.grm"));
		}
		File selFile = model.getSaveFile();
		saveGrimPanData(selFile);	
	}
	public void saveAsAction() {
		if (frameView.jFileChooser2.showSaveDialog(frameView) ==
				JFileChooser.APPROVE_OPTION) {
			File selFile = frameView.jFileChooser2.getSelectedFile();
			model.setSaveFile(selFile);
			saveGrimPanData(selFile);
		}
	}
	public void readShapeFromSaveFile(File saveFile) {
		model.setSaveFile(saveFile);
		ObjectInputStream input;
		try {
			input =
				new ObjectInputStream(new FileInputStream(saveFile));
			model.shapeList = (ArrayList<GrimShape>) input.readObject();
			input.close();

		} catch (ClassNotFoundException e) {
			System.err.println("Class not Found");
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.err.println("File not Found");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IO Exception");
			e.printStackTrace();
		}
	}
	public void saveGrimPanData(File saveFile){
		ObjectOutputStream output;
		try {
			output = new ObjectOutputStream(new FileOutputStream(saveFile));
			output.writeObject(model.shapeList);
			output.close();
		} catch (FileNotFoundException e) {
			System.err.println("File not Found");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IO Exception");
			e.printStackTrace();
		}
	}

	/**
	 * @return the drawPanelView
	 */
	public DrawPanelView getDrawPanelView() {
		return drawPanelView;
	}

	/**
	 * @param drawPanelView the drawPanelView to set
	 */
	public void setDrawPanelView(DrawPanelView drawPanelView) {
		this.drawPanelView = drawPanelView;
	}

	/**
	 * 
	 */
	public void clearAllShape() {
		model.shapeList.clear();
		model.curDrawShape = null;
		model.polygonPoints.clear();
		drawPanelView.repaint();
	}

	/**
	 * 
	 */
	public void setMoveShapeState() {
		model.setEditState(model.STATE_MOVE);
		if (model.curDrawShape != null){
			model.shapeList
			.add(new GrimShape(model.curDrawShape, model.getShapeStrokeWidth(),
					model.getShapeStrokeColor(), model.isShapeFill(), model.getShapeFillColor()));
			model.curDrawShape = null;
		}
		drawPanelView.repaint();
	}
	/**
	 * 
	 */
	public void setAddShapeState() {
		model.setEditState(model.savedAddState);
		
	}

	/**
	 * 
	 */
	public void setStrokeWithAction() {
		String strSW = model.grimpanPM.getPanProperties().getProperty("default.stroke.width");
		String inputVal = JOptionPane.showInputDialog("���β�", strSW);
		if (inputVal!=null){
			model.setShapeStrokeWidth(Float.parseFloat(inputVal));
			strSW = inputVal;
		}
		else {
			model.setShapeStrokeWidth(Float.parseFloat(strSW));
		}
		model.grimpanPM.updatePropertyOfXML("default.stroke.width", strSW);
		
		
	}

	/**
	 * 
	 */
	public void setBoundaryColorAction() {
		String strColor = model.grimpanPM.getPanProperties().getProperty("default.stroke.color");
		Color defColor = new Color(Integer.parseInt(strColor, 16));

		Color color = 
				JColorChooser.showDialog(frameView, 
						"Choose a color",
						defColor);					
		if (color!=null){
			model.setShapeStrokeColor(color);
			defColor = color;
		}
		else {
			model.setShapeStrokeColor(defColor);
		}
		strColor = String.format("%06X", (0xFFFFFF & defColor.getRGB()));
		System.out.println("Color="+strColor+"=");
		model.grimpanPM.updatePropertyOfXML("default.stroke.color", strColor);
	}

	/**
	 * 
	 */
	public void setFillColorAction() {
		Color color = 
				JColorChooser.showDialog(frameView, 
						"Choose a color",
						Color.black);					
		if (color!=null){
			model.setShapeFillColor(color);
		}
		else {
			model.setShapeFillColor(Color.black);
		}
	}

	/**
	 * 
	 */
	public void addShapeAction() {
		Command addCommand = new AddCommand(model, new GrimShape(model.curDrawShape, model.getShapeStrokeWidth(),
				model.getShapeStrokeColor(), model.isShapeFill(), model.getShapeFillColor()));
		model.undoCommandStack.push(addCommand);// save for undo
		addCommand.execute();
		frameView.countLbl.setText(String.format("Shape Count: %d ", GrimPanFrameMain.shapecount));

	}

	/**
	 * 
	 */
	public void moveShapeAction() {
		Command moveCommand = new MoveCommand(model, model.getSavedPositionShape());
		model.undoCommandStack.push(moveCommand);// save for undo
		moveCommand.execute();
	}
	/**
	 * 
	 */
	public void recoveryAction() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 */
	public void undoAction() {
		Command comm = model.undoCommandStack.pop();
		comm.undo();
		drawPanelView.repaint();
		
		if(comm instanceof AddCommand)
			frameView.countLbl.setText(String.format("Shape Count: %d ", GrimPanFrameMain.shapecount));
	}




}
