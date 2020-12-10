

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.JPanel;

public class GraphicsDisplay extends JPanel {
    int i=0;
	private ArrayList<Double[]> graphicsData;
	private ArrayList<Double[]> originalData;
	private int selectedMarker = -1;
	private double minX;
	private double maxX;
	private double minY;
	private double maxY;
	
	private double scaleX;
	private double scaleY;
	
	private double[][] viewport = new double[2][2];
	private ArrayList<double[][]> undoHistory = new ArrayList(5);
	private boolean showAxis = true;
	private boolean showMarkers = true;
	private boolean clockRotate = false;
	private boolean antiClockRotate = false;

	private Font axisFont;
	private Font labelsFont;
	
	private BasicStroke axisStroke;
	//private BasicStroke graphicsStroke;
	private BasicStroke markerStroke;
	private BasicStroke gridStroke;
	private BasicStroke selectionStroke;
	private static DecimalFormat formatter=(DecimalFormat)NumberFormat.getInstance();
	
	private boolean scaleMode = false;
	private boolean changeMode = false;
	private double[] originalPoint = new double[2];
	private Rectangle2D.Double selectionRect = new Rectangle2D.Double();
	
	public GraphicsDisplay ()	{
		setBackground(Color.WHITE);
		/*graphicsStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, 
				new float [] {4,1,1,1,2,1,1,1,4}, 0.0f);*/
		axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, null, 0.0f);
		markerStroke = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 5.0f, null, 0.0f);
		selectionStroke = new BasicStroke(1.0F, 0, 0, 10.0F, new float[] { 10, 10 }, 0.0F);		
		gridStroke = new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 5.0f, new float [] {5,5}, 2.0f);
		axisFont = new Font("Serif", Font.BOLD, 36);
		labelsFont = new java.awt.Font("Serif",0,10);
		addMouseMotionListener(new MouseMotionHandler());
		addMouseListener(new MouseHandler());
	}
	
	public void showGraphics(ArrayList<Double[]> graphicsData)	{
		this.graphicsData = graphicsData;
		

	    this.originalData = new ArrayList(graphicsData.size());
	    for (Double[] point : graphicsData) {
	      Double[] newPoint = new Double[2];
	      newPoint[0] = new Double(point[0].doubleValue());
	      newPoint[1] = new Double(point[1].doubleValue());
	      this.originalData.add(newPoint);
	    }
	    this.minX = ((Double[])graphicsData.get(0))[0].doubleValue();
	    this.maxX = ((Double[])graphicsData.get(graphicsData.size() - 1))[0].doubleValue();
	    this.minY = ((Double[])graphicsData.get(0))[1].doubleValue();
	    this.maxY = this.minY;
		
	    for (int i = 1; i < graphicsData.size(); i++) {
	        if (((Double[])graphicsData.get(i))[1].doubleValue() < this.minY) {
	          this.minY = ((Double[])graphicsData.get(i))[1].doubleValue();
	        }
	        if (((Double[])graphicsData.get(i))[1].doubleValue() > this.maxY) {
	          this.maxY = ((Double[])graphicsData.get(i))[1].doubleValue();
	        }
	    }
		
		zoomToRegion(minX, maxY, maxX, minY);
		
		}
	
	public void zoomToRegion(double x1,double y1,double x2,double y2)	{
		this.viewport[0][0]=x1;
		this.viewport[0][1]=y1;
		this.viewport[1][0]=x2;
		this.viewport[1][1]=y2;
		this.repaint();
	}
	public void setShowAxis(boolean showAxis) {
		this.showAxis = showAxis;
		repaint();
	}

	public void setShowMarkers(boolean showMarkers) {
		this.showMarkers = showMarkers;
		repaint();
	}
	
	protected Point2D.Double xyToPoint(double x, double y) {
		double deltaX = x - viewport[0][0];
		double deltaY = viewport[0][1] - y;
		return new Point2D.Double(deltaX*scaleX, deltaY*scaleY);
	}
	 
	protected double[] translatePointToXY(int x, int y)
	  {
	    return new double[] { this.viewport[0][0] + x / this.scaleX, this.viewport[0][1] - y / this.scaleY };
	  }
		
	protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
		Point2D.Double dest = new Point2D.Double();
		dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
		return dest;
	}
	
	protected void paintGrid (Graphics2D canvas) {
		canvas.setStroke(gridStroke);
		canvas.setColor(Color.GRAY);
		// Сетка
		double pos = viewport[0][0];;
		double step = (viewport[1][0] - viewport[0][0])/10;
		
		while (pos < viewport[1][0]){
			canvas.draw(new Line2D.Double(xyToPoint(pos, viewport[0][1]), xyToPoint(pos, viewport[1][1])));
			pos += step;
			}
		canvas.draw(new Line2D.Double(xyToPoint(viewport[1][0],viewport[0][1]), xyToPoint(viewport[1][0],viewport[1][1])));
		
		pos = viewport[1][1];
		step = (viewport[0][1] - viewport[1][1]) / 10;
		while (pos < viewport[0][1]){
			canvas.draw(new Line2D.Double(xyToPoint(viewport[0][0], pos), xyToPoint(viewport[1][0], pos)));
			pos=pos + step;
			}
		canvas.draw(new Line2D.Double(xyToPoint(viewport[0][0],viewport[0][1]), xyToPoint(viewport[1][0],viewport[0][1])));
	}
	
	protected void paintGraphics (Graphics2D canvas) {
		canvas.setStroke(new BasicStroke(3f));
	    canvas.setColor(Color.RED);
        //System.out.println(count);
	    // Линии
	    Double currentX = null;
	    Double currentY = null;
        Double currentX1 = null;
        Double currentY1 = null;
        if(i==0)
        {
            i=graphicsData.size();
        }
        int a=0;
            for (Double[] point : this.graphicsData) {
                if(a<i) {
                    if ((point[0].doubleValue() >= this.viewport[0][0]) && (point[1].doubleValue() <= this.viewport[0][1]) &&
                            (point[0].doubleValue() <= this.viewport[1][0]) && (point[1].doubleValue() >= this.viewport[1][1])) {
                        if ((currentX != null) && (currentY != null)) {
                            //canvas.draw(new Line2D.Double(xyToPoint(currentX.doubleValue(), currentY.doubleValue()),
                            //xyToPoint(point[0].doubleValue()+0.2, point[1].doubleValue())));
                            Double diffX = xyToPoint(currentX, currentY).getX() - xyToPoint(point[0], point[1]).getX();
                            Double diffY = xyToPoint(currentX, currentY).getY() - xyToPoint(point[0], point[1]).getY();
                            //System.out.println(diffX+" "+diffY);
                            GeneralPath line = new GeneralPath();
                            Point2D.Double center = xyToPoint(point[0], point[1]);
                            line.moveTo(center.getX(), center.getY());
                            line.lineTo(line.getCurrentPoint().getX() + (diffX / 3.5), line.getCurrentPoint().getY() + (diffY / 3.5));
                            canvas.draw(line);
                            canvas.fill(line);
                            GeneralPath line1 = new GeneralPath();
                            canvas.setColor(Color.WHITE);
                            line1.moveTo(line.getCurrentPoint().getX(), line.getCurrentPoint().getY());
                            line1.lineTo(line1.getCurrentPoint().getX() + (diffX / 7), line1.getCurrentPoint().getY() + (diffY / 7));
                            canvas.draw(line1);
                            canvas.fill(line1);
                            GeneralPath line2 = new GeneralPath();
                            canvas.setColor(Color.RED);
                            line2.moveTo(line1.getCurrentPoint().getX(), line1.getCurrentPoint().getY());
                            line2.lineTo(line2.getCurrentPoint().getX() + (diffX / 3.5), line2.getCurrentPoint().getY() + (diffY / 3.5));
                            canvas.draw(line2);
                            canvas.fill(line2);
                            GeneralPath line3 = new GeneralPath();
                            canvas.setColor(Color.white);
                            line3.moveTo(line2.getCurrentPoint().getX(), line2.getCurrentPoint().getY());
                            line3.lineTo(line3.getCurrentPoint().getX() + (diffX / 7), line3.getCurrentPoint().getY() + (diffY / 7));
                            canvas.draw(line3);
                            canvas.fill(line3);
                            GeneralPath line4 = new GeneralPath();
                            canvas.setColor(Color.RED);
                            line4.moveTo(line3.getCurrentPoint().getX(), line3.getCurrentPoint().getY());
                            line4.lineTo(line4.getCurrentPoint().getX() + (diffX / 7), line4.getCurrentPoint().getY() + (diffY / 7));
                            canvas.draw(line4);
                            canvas.fill(line4);
                        }
                        currentX = point[0];
                        currentY = point[1];
                    }
                }
                if(a>=i) {
                        if ((point[0].doubleValue() >= this.viewport[0][0]) && (point[1].doubleValue() <= this.viewport[0][1]) &&
                                (point[0].doubleValue() <= this.viewport[1][0]) && (point[1].doubleValue() >= this.viewport[1][1]))
                        {
                            if ((currentX1 != null) && (currentY1 != null)) {
                                //canvas.draw(new Line2D.Double(xyToPoint(currentX.doubleValue(), currentY.doubleValue()),
                                //xyToPoint(point[0].doubleValue()+0.2, point[1].doubleValue())));
                                Double diffX=xyToPoint(currentX1,currentY1).getX()-xyToPoint(point[0],point[1]).getX();
                                Double diffY=xyToPoint(currentX1,currentY1).getY()-xyToPoint(point[0],point[1]).getY();
                                //System.out.println(diffX+" "+diffY);
                                GeneralPath line = new GeneralPath();
                                Point2D.Double center = xyToPoint(point[0], point[1]);
                                line.moveTo(center.getX(),center.getY());
                                line.lineTo(line.getCurrentPoint().getX()+(diffX/3.5),line.getCurrentPoint().getY()+(diffY/3.5));
                                canvas.draw(line);
                                canvas.fill(line);
                                GeneralPath line1 = new GeneralPath();
                                canvas.setColor(Color.WHITE);
                                line1.moveTo(line.getCurrentPoint().getX(),line.getCurrentPoint().getY());
                                line1.lineTo(line1.getCurrentPoint().getX()+(diffX/7),line1.getCurrentPoint().getY()+(diffY/7));
                                canvas.draw(line1);
                                canvas.fill(line1);
                                GeneralPath line2 = new GeneralPath();
                                canvas.setColor(Color.RED);
                                line2.moveTo(line1.getCurrentPoint().getX(),line1.getCurrentPoint().getY());
                                line2.lineTo(line2.getCurrentPoint().getX()+(diffX/3.5),line2.getCurrentPoint().getY()+(diffY/3.5));
                                canvas.draw(line2);
                                canvas.fill(line2);
                                GeneralPath line3 = new GeneralPath();
                                canvas.setColor(Color.white);
                                line3.moveTo(line2.getCurrentPoint().getX(),line2.getCurrentPoint().getY());
                                line3.lineTo(line3.getCurrentPoint().getX()+(diffX/7),line3.getCurrentPoint().getY()+(diffY/7));
                                canvas.draw(line3);
                                canvas.fill(line3);
                                GeneralPath line4 = new GeneralPath();
                                canvas.setColor(Color.RED);
                                line4.moveTo(line3.getCurrentPoint().getX(),line3.getCurrentPoint().getY());
                                line4.lineTo(line4.getCurrentPoint().getX()+(diffX/7),line4.getCurrentPoint().getY()+(diffY/7));
                                canvas.draw(line4);
                                canvas.fill(line4);
                            }
                            currentX1 = point[0];
                            currentY1 = point[1];
                        }
                }
                a++;
            }
	    /*if(this.graphicsData.size()<=100&&graphicsData.size()>=50)
        {
            for (Double[] point : this.graphicsData)
                if ((point[50].doubleValue() >= this.viewport[0][0]) && (point[51].doubleValue() <= this.viewport[0][1]) &&
                        (point[50].doubleValue() <= this.viewport[1][0]) && (point[51].doubleValue() >= this.viewport[1][1]))
                {
                    if ((currentX1 != null) && (currentY1 != null)) {
                        //canvas.draw(new Line2D.Double(xyToPoint(currentX.doubleValue(), currentY.doubleValue()),
                        //xyToPoint(point[0].doubleValue()+0.2, point[1].doubleValue())));
                        Double diffX=xyToPoint(currentX1,currentY1).getX()-xyToPoint(point[50],point[51]).getX();
                        Double diffY=xyToPoint(currentX1,currentY1).getY()-xyToPoint(point[50],point[51]).getY();
                        //System.out.println(diffX+" "+diffY);
                        GeneralPath line = new GeneralPath();
                        Point2D.Double center = xyToPoint(point[50], point[51]);
                        line.moveTo(center.getX(),center.getY());
                        line.lineTo(line.getCurrentPoint().getX()+(diffX/3.5),line.getCurrentPoint().getY()+(diffY/3.5));
                        canvas.draw(line);
                        canvas.fill(line);
                        GeneralPath line1 = new GeneralPath();
                        canvas.setColor(Color.WHITE);
                        line1.moveTo(line.getCurrentPoint().getX(),line.getCurrentPoint().getY());
                        line1.lineTo(line1.getCurrentPoint().getX()+(diffX/7),line1.getCurrentPoint().getY()+(diffY/7));
                        canvas.draw(line1);
                        canvas.fill(line1);
                        GeneralPath line2 = new GeneralPath();
                        canvas.setColor(Color.RED);
                        line2.moveTo(line1.getCurrentPoint().getX(),line1.getCurrentPoint().getY());
                        line2.lineTo(line2.getCurrentPoint().getX()+(diffX/3.5),line2.getCurrentPoint().getY()+(diffY/3.5));
                        canvas.draw(line2);
                        canvas.fill(line2);
                        GeneralPath line3 = new GeneralPath();
                        canvas.setColor(Color.white);
                        line3.moveTo(line2.getCurrentPoint().getX(),line2.getCurrentPoint().getY());
                        line3.lineTo(line3.getCurrentPoint().getX()+(diffX/7),line3.getCurrentPoint().getY()+(diffY/7));
                        canvas.draw(line3);
                        canvas.fill(line3);
                        GeneralPath line4 = new GeneralPath();
                        canvas.setColor(Color.RED);
                        line4.moveTo(line3.getCurrentPoint().getX(),line3.getCurrentPoint().getY());
                        line4.lineTo(line4.getCurrentPoint().getX()+(diffX/7),line4.getCurrentPoint().getY()+(diffY/7));
                        canvas.draw(line4);
                        canvas.fill(line4);
                    }
                    currentX1 = point[2];
                    currentY1 = point[3];
                }
            }*/
        }

	
	protected void paintAxis(Graphics2D canvas){
		// Оси
		canvas.setStroke(this.axisStroke);
		canvas.setColor(java.awt.Color.BLACK);
		canvas.setFont(this.axisFont);
		FontRenderContext context=canvas.getFontRenderContext();
		if (!(viewport[0][0] > 0|| viewport[1][0] < 0)){
			canvas.draw(new Line2D.Double(xyToPoint(0, viewport[0][1]),
					xyToPoint(0, viewport[1][1])));
			canvas.draw(new Line2D.Double(xyToPoint(-(viewport[1][0] - viewport[0][0]) * 0.0025,
					viewport[0][1] - (viewport[0][1] - viewport[1][1]) * 0.015),xyToPoint(0,viewport[0][1])));
			canvas.draw(new Line2D.Double(xyToPoint((viewport[1][0] - viewport[0][0]) * 0.0025,
					viewport[0][1] - (viewport[0][1] - viewport[1][1]) * 0.015),
					xyToPoint(0, viewport[0][1])));
			Rectangle2D bounds = axisFont.getStringBounds("y",context);
			Point2D.Double labelPos = xyToPoint(0.0, viewport[0][1]);
			canvas.drawString("y",(float)labelPos.x + 10,(float)(labelPos.y + bounds.getHeight() / 2));
			}
		if (!(viewport[1][1] > 0.0D || viewport[0][1] < 0.0D)){
			canvas.draw(new Line2D.Double(xyToPoint(viewport[0][0],0),
					xyToPoint(viewport[1][0],0)));
			canvas.draw(new Line2D.Double(xyToPoint(viewport[1][0] - (viewport[1][0] - viewport[0][0]) * 0,
					(viewport[0][1] - viewport[1][1]) * 0.005), xyToPoint(viewport[1][0], 0)));
			canvas.draw(new Line2D.Double(xyToPoint(viewport[1][0] - (viewport[1][0] - viewport[0][0]) * 0.01,
					-(viewport[0][1] - viewport[1][1]) * 0.005), xyToPoint(viewport[1][0], 0)));
			Rectangle2D bounds = axisFont.getStringBounds("x",context);
			Point2D.Double labelPos = xyToPoint(this.viewport[1][0],0.0D);
			canvas.drawString("x",(float)(labelPos.x - bounds.getWidth() - 10),(float)(labelPos.y - bounds.getHeight() / 2));
			}
	}
	
	protected void paintMarkers(Graphics2D canvas) {
		canvas.setStroke(this.markerStroke);
	    canvas.setColor(Color.red);
	    canvas.setPaint(Color.RED);
	    GeneralPath lastMarker = null;
	    int i = -1;
	    for (Double[] point : graphicsData) {
	      i++;


	      
			// Маркеры
	        GeneralPath star = new GeneralPath();
			Point2D.Double center = xyToPoint(point[0], point[1]);
			if(isSpecialPoint(center.getX()))
				canvas.setColor(Color.GREEN);
			else
				canvas.setColor(Color.RED);
			star.moveTo(center.getX(),center.getY());
			star.lineTo(star.getCurrentPoint().getX(),star.getCurrentPoint().getY()+10);
			star.moveTo(center.getX(),center.getY());
			star.lineTo(star.getCurrentPoint().getX(),star.getCurrentPoint().getY()-10);
			star.moveTo(center.getX(),center.getY());
			star.lineTo(star.getCurrentPoint().getX()+10,star.getCurrentPoint().getY());
			star.moveTo(center.getX(),center.getY());
			star.lineTo(star.getCurrentPoint().getX()-10,star.getCurrentPoint().getY());
			star.moveTo(center.getX(),center.getY()+10);
			star.lineTo(star.getCurrentPoint().getX()+5,star.getCurrentPoint().getY());
			star.moveTo(star.getCurrentPoint().getX(),star.getCurrentPoint().getY());
			star.lineTo(star.getCurrentPoint().getX()+5,star.getCurrentPoint().getY()-5);
			star.moveTo(star.getCurrentPoint().getX(),star.getCurrentPoint().getY());
			star.lineTo(center.getX()+10,center.getY());
			star.moveTo(star.getCurrentPoint().getX(),star.getCurrentPoint().getY());
			star.lineTo(star.getCurrentPoint().getX(),star.getCurrentPoint().getY()-5);
			star.moveTo(star.getCurrentPoint().getX(),star.getCurrentPoint().getY());
			star.lineTo(star.getCurrentPoint().getX()-5,star.getCurrentPoint().getY()-5);
			star.moveTo(star.getCurrentPoint().getX(),star.getCurrentPoint().getY());
			star.lineTo(center.getX(),center.getY()-10);
			star.moveTo(star.getCurrentPoint().getX(),star.getCurrentPoint().getY());
			star.lineTo(star.getCurrentPoint().getX()-5,star.getCurrentPoint().getY());
			star.moveTo(star.getCurrentPoint().getX(),star.getCurrentPoint().getY());
			star.lineTo(star.getCurrentPoint().getX()-5,star.getCurrentPoint().getY()+5);
			star.moveTo(star.getCurrentPoint().getX(),star.getCurrentPoint().getY());
			star.lineTo(center.getX()-10,center.getY());
			star.moveTo(star.getCurrentPoint().getX(),star.getCurrentPoint().getY());
			star.lineTo(star.getCurrentPoint().getX(),star.getCurrentPoint().getY()+5);
			star.moveTo(star.getCurrentPoint().getX(),star.getCurrentPoint().getY());
			star.lineTo(star.getCurrentPoint().getX()+5,star.getCurrentPoint().getY()+5);
			star.moveTo(star.getCurrentPoint().getX(),star.getCurrentPoint().getY());
			star.lineTo(center.getX(),center.getY()+10);
	        if (i == this.selectedMarker)
	        {
	          lastMarker = star;

	        }
	        else {
	          canvas.draw(star);
	          canvas.fill(star);
	        }
	      }
	    
	    if (lastMarker != null) {
	     canvas.setColor(Color.BLUE);
	      canvas.setPaint(Color.BLUE);
	      canvas.draw(lastMarker);
	      canvas.fill(lastMarker);
	    }
	}
	
	protected boolean isSpecialPoint(double y){
		//раскраска маркеров по условию
		int Yint = (int)y;
		boolean flag;
		int sum=0;
		String s=Integer.toString(Yint);
		int i=0;
		while (Yint>0)
		{
		    int ost=Yint%10;
		    Yint=Yint/10;
			sum=sum+ost;
			i++;
		}
		if(sum<10)
		{
			flag=true;
		}
		else
		{
			flag=false;
		}
		return flag;
	}



	private void paintLabels(Graphics2D canvas){
		// Подписи координат и сетки
		canvas.setColor(Color.BLACK);
		canvas.setFont(this.labelsFont);
		FontRenderContext context=canvas.getFontRenderContext();
		double labelYPos;
		double labelXPos;
		if (!(viewport[1][1] >= 0 || viewport[0][1] <= 0))
			labelYPos = 0;
		else labelYPos = viewport[1][1];
		if (!(viewport[0][0] >= 0 || viewport[1][0] <= 0.0D))
			labelXPos=0;
		else labelXPos = viewport[0][0];
		double pos = viewport[0][0];
		double step = (viewport[1][0] - viewport[0][0]) / 10;
		while (pos < viewport[1][0]){
			java.awt.geom.Point2D.Double point = xyToPoint(pos,labelYPos);
			String label = formatter.format(pos);
			Rectangle2D bounds = labelsFont.getStringBounds(label,context);
			canvas.drawString(label, (float)(point.getX() + 5), (float)(point.getY() - bounds.getHeight()));
			pos=pos + step;
			}
		pos = viewport[1][1];
		step = (viewport[0][1] - viewport[1][1]) / 10.0D;
		while (pos < viewport[0][1]){
			Point2D.Double point = xyToPoint(labelXPos,pos);
			String label=formatter.format(pos);
			Rectangle2D bounds = labelsFont.getStringBounds(label,context);
			canvas.drawString(label,(float)(point.getX() + 5),(float)(point.getY() - bounds.getHeight()));
			pos=pos + step;
			}
		if (selectedMarker >= 0)
	    {
	      Point2D.Double point = xyToPoint(((Double[])graphicsData.get(selectedMarker))[0].doubleValue(), 
	    		  ((Double[])graphicsData.get(selectedMarker))[1].doubleValue());
	      String label = "X=" + formatter.format(((Double[])graphicsData.get(selectedMarker))[0]) + 
	    		  ", Y=" + formatter.format(((Double[])graphicsData.get(selectedMarker))[1]);
	      Rectangle2D bounds = labelsFont.getStringBounds(label, context);
	      canvas.setColor(Color.BLACK);
	      canvas.drawString(label, (float)(point.getX() + 5.0D), (float)(point.getY() - bounds.getHeight()));
	    }
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		scaleX=this.getSize().getWidth() / (this.viewport[1][0] - this.viewport[0][0]);
		scaleY=this.getSize().getHeight() / (this.viewport[0][1] - this.viewport[1][1]);
		if ((this.graphicsData == null) || (this.graphicsData.size() == 0)) return;
	

		Graphics2D canvas = (Graphics2D) g;
		Stroke oldStroke = canvas.getStroke();
		Color oldColor = canvas.getColor();
		Font oldFont = canvas.getFont();
		Paint oldPaint = canvas.getPaint();
		/// поворот
		if (clockRotate) {
			AffineTransform at = AffineTransform.getRotateInstance(Math.PI/2, getSize().getWidth()/2, getSize().getHeight()/2); 
			at.concatenate(new AffineTransform(getSize().getHeight()/getSize().getWidth(), 0.0, 0.0, getSize().getWidth()/getSize().getHeight(),
					(getSize().getWidth()-getSize().getHeight())/2, (getSize().getHeight()-getSize().getWidth())/2)); 
			canvas.setTransform(at);
			
		}
		if (antiClockRotate) {
			AffineTransform at = AffineTransform.getRotateInstance(-Math.PI/2, getSize().getWidth()/2, getSize().getHeight()/2); 
			at.concatenate(new AffineTransform(getSize().getHeight()/getSize().getWidth(), 0.0, 0.0, getSize().getWidth()/getSize().getHeight(),
					(getSize().getWidth()-getSize().getHeight())/2, (getSize().getHeight()-getSize().getWidth())/2)); 
			canvas.setTransform(at);
			
			
		}
		paintGrid(canvas);
		if (showAxis) 
			{paintAxis(canvas);
			paintLabels(canvas);
			}
		paintGraphics(canvas);
		if (showMarkers) paintMarkers(canvas);
		
		paintSelection(canvas);
		canvas.setFont(oldFont);
		canvas.setPaint(oldPaint);
		canvas.setColor(oldColor);
		canvas.setStroke(oldStroke);
		
	}
	
	private void paintSelection(Graphics2D canvas) {
	    if (!scaleMode) return;
	    canvas.setStroke(selectionStroke);
	    canvas.setColor(Color.BLACK);
	    canvas.draw(selectionRect);
	  }

	// Устанавливаем значения по часовой стрелки
	public void setClockRotate(boolean clockRotate) {
		this.clockRotate = clockRotate;
		repaint();
	}
	
	// Устанавливаем значения проив часовой стрелки
	public void setAntiClockRotate(boolean antiClockRotate) {
		this.antiClockRotate = antiClockRotate;
		repaint();
	}
	
	// По часовой стрелке
	public boolean isClockRotate() {
		return clockRotate;
	}

	// Поворот против часовой стрелки
	public boolean isAntiClockRotate() {
		return antiClockRotate;
	}
	
	// Сбрасываем изменения
	public void reset() {
		showGraphics(this.originalData);
	}
	
	//Приближаем
	protected int findSelectedPoint(int x, int y)
	  {
	    if (graphicsData == null) return -1;
	    int pos = 0;
	    for (Double[] point : graphicsData) {
	      Point2D.Double screenPoint = xyToPoint(point[0].doubleValue(), point[1].doubleValue());
	      double distance = (screenPoint.getX() - x) * (screenPoint.getX() - x) + (screenPoint.getY() - y) * (screenPoint.getY() - y);
	      if (distance < 100) return pos;
	      pos++;
	    }	    return -1;
	  }
	
	 public void saveToTextFile(File selectedFile)	{
			try{
				PrintStream out = new PrintStream(selectedFile);
				out.println("Результаты скорректированых значений");
				for (Double[] point : graphicsData){
					out.println(point[0] + " " + point[1]);
				}
				
				out.close();
			
			}catch (FileNotFoundException e){
			
			}
		
		}
	 
	 
	 public class MouseHandler extends MouseAdapter {
		    public MouseHandler() {
		    }
		    public void mouseClicked(MouseEvent ev) {
		      if (ev.getButton() == 3) {
		        if (undoHistory.size() > 0)
		        {
		          viewport = ((double[][])undoHistory.get(undoHistory.size() - 1));

		          undoHistory.remove(undoHistory.size() - 1);
		        } else {
		          zoomToRegion(minX, maxY, maxX, minY);
		        }
		        repaint();
		      }
		    }

		    public void mousePressed(MouseEvent ev) {
		      if (ev.getButton() != 1) return;
		      selectedMarker = findSelectedPoint(ev.getX(), ev.getY());
		      originalPoint = translatePointToXY(ev.getX(), ev.getY());
		      if (selectedMarker >= 0) {
		        changeMode = true;
		        setCursor(Cursor.getPredefinedCursor(8));
		      } else {
		        scaleMode = true;
		        setCursor(Cursor.getPredefinedCursor(5));
		        selectionRect.setFrame(ev.getX(), ev.getY(), 1.0D, 1.0D);
		      }
		    }

		    public void mouseReleased(MouseEvent ev) {
		      if (ev.getButton() != 1) return;

		      setCursor(Cursor.getPredefinedCursor(0));
		      if (changeMode) {
		        changeMode = false;
		      } else {
		        scaleMode = false;
		        double[] finalPoint = translatePointToXY(ev.getX(), ev.getY());
		        undoHistory.add(viewport);
		        viewport = new double[2][2];
		        zoomToRegion(originalPoint[0], originalPoint[1], finalPoint[0], finalPoint[1]);
		        repaint();
		      }
		    }
		  }
	 
	 // Оброботчик движения мыши
	 public class MouseMotionHandler implements MouseMotionListener {
	
		 public void mouseDragged(MouseEvent ev) {
			 if (changeMode) {
				//Добавить поворот (при)
				 double[] currentPoint = translatePointToXY(ev.getX(), ev.getY());
				 double newY = ((Double[])graphicsData.get(selectedMarker))[1].doubleValue() + 
						 (currentPoint[1] - ((Double[])graphicsData.get(selectedMarker))[1].doubleValue());
				 if (newY > viewport[0][1]) {
					 newY = viewport[0][1];
				 }
				 if (newY < viewport[1][1]) {
					 newY = viewport[1][1];
				 }
				 ((Double[])graphicsData.get(selectedMarker))[1] = Double.valueOf(newY);
				 repaint();
			 } else {
				 double width = ev.getX() - selectionRect.getX();
				 if (width < 5.0D) {
					 width = 5.0D;
				 }
	        double height = ev.getY() - selectionRect.getY();
	        if (height < 5.0D) {
	          height = 5.0D;
	        }
	        selectionRect.setFrame(selectionRect.getX(), selectionRect.getY(), width, height);
	        repaint();
	      }
	}

		 //перемещения мыши
	public void mouseMoved(MouseEvent ev) {
		selectedMarker = findSelectedPoint(ev.getX(), ev.getY());
	      if (selectedMarker >= 0)
	        setCursor(Cursor.getPredefinedCursor(8));
	      else {
	    	  setCursor(Cursor.getPredefinedCursor(0));
	      }
	      repaint();
	}
	
}
	 
}
