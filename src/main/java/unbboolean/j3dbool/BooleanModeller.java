package unbboolean.j3dbool;

import java.util.ArrayList;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;

/**
 * Class used to apply boolean operations on solids.
 * 
 * <br><br>Two 'Solid' objects are submitted to this class constructor. There is a methods for 
 * each boolean operation. Each of these return a 'Solid' resulting from the application
 * of its operation into the submitted solids. 
 *  
 * <br><br>See: D. H. Laidlaw, W. B. Trumbore, and J. F. Hughes.  
 * "Constructive Solid Geometry for Polyhedral Objects" 
 * SIGGRAPH Proceedings, 1986, p.161.
 *  
 * @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 */
public class BooleanModeller implements Cloneable
{
	/** solid where boolean operations will be applied */
	private Object3D object1, object2;
	
	//--------------------------------CONSTRUCTORS----------------------------------//
	
	/**
	 * Constructs a BooleanModeller object to apply boolean operation in two solids. 
	 * Makes preliminary calculations 
	 * 
	 * @param solid1 first solid where boolean operations will be applied
	 * @param solid2 second solid where boolean operations will be applied
	 */	
	public BooleanModeller(Solid solid1, Solid solid2)
	{
		//representation to apply boolean operations
		object1 = new Object3D(solid1);
		object2 = new Object3D(solid2);
		
		//split the faces so that none of them intercepts each other
		object1.splitFaces(object2);
		object2.splitFaces(object1);
				
		//classify faces as being inside or outside the other solid
		object1.classifyFaces(object2);
		object2.classifyFaces(object1);
	}
	
	//----------------------------------OVERRIDES-----------------------------------//
	
	/**
	 * Clones the BooleanModeller object
	 * 
	 * @return cloned BooleanModeller object
	 */
	public Object clone()
	{
		try
		{
			BooleanModeller clone = (BooleanModeller)super.clone();
			clone.object1 = (Object3D)object1.clone();
			clone.object2 = (Object3D)object2.clone();
			return clone;
		}
		catch(CloneNotSupportedException e)
		{	
			return null;
		}
	}
				
	//-------------------------------BOOLEAN_OPERATIONS-----------------------------//
	
	/**
	 * Gets the solid generated by the union of the two solids submitted to the constructor
	 * 
	 * @return solid generated by the union of the two solids submitted to the constructor
	 */
	public Solid getUnion() 
	{
		return composeSolid(Face.OUTSIDE, Face.SAME, Face.OUTSIDE);
	}
	
	/**
	 * Gets the solid generated by the intersection of the two solids submitted to the constructor
	 * 
	 * @return solid generated by the intersection of the two solids submitted to the constructor.
	 * The generated solid may be empty depending on the solids. In this case, it can't be used on a scene
	 * graph. To check this, use the Solid.isEmpty() method.
	 */
	public Solid getIntersection()
	{
		return composeSolid(Face.INSIDE, Face.SAME, Face.INSIDE);
	}
	
	/** Gets the solid generated by the difference of the two solids submitted to the constructor. 
	 * The fist solid is substracted by the second. 
	 * 
	 * @return solid generated by the difference of the two solids submitted to the constructor
	 */
	public Solid getDifference()
	{
		object2.invertInsideFaces();
		Solid result = composeSolid(Face.OUTSIDE, Face.OPPOSITE, Face.INSIDE);
		object2.invertInsideFaces();
		
		return result;
	}
	
	//--------------------------PRIVATES--------------------------------------------//
	
	/**
	 * Composes a solid based on the faces status of the two operators solids:
	 * Face.INSIDE, Face.OUTSIDE, Face.SAME, Face.OPPOSITE
	 * 
	 * @param faceStatus1 status expected for the first solid faces
	 * @param faceStatus2 other status expected for the first solid faces
	 * (expected a status for the faces coincident with second solid faces)
	 * @param faceStatus3 status expected for the second solid faces
	 */
	private Solid composeSolid(int faceStatus1, int faceStatus2, int faceStatus3) 
	{
		ArrayList vertices = new ArrayList();
		ArrayList indices = new ArrayList();
		ArrayList colors = new ArrayList();
		
		//group the elements of the two solids whose faces fit with the desired status  
		groupObjectComponents(object1, vertices, indices, colors, faceStatus1, faceStatus2);
		groupObjectComponents(object2, vertices, indices, colors, faceStatus3, faceStatus3);
		
		//turn the arrayLists to arrays
		Point3d[] verticesArray = new Point3d[vertices.size()];
		for(int i=0;i<vertices.size();i++)
		{
			verticesArray[i] = ((Vertex)vertices.get(i)).getPosition();
		}
		int[] indicesArray = new int[indices.size()];
		for(int i=0;i<indices.size();i++)
		{
			indicesArray[i] = ((Integer)indices.get(i)).intValue();
		}
		Color3f[] colorsArray = new Color3f[colors.size()];
		for(int i=0;i<colors.size();i++)
		{
			colorsArray[i] = (Color3f)((Color3f)colors.get(i)).clone();
		}
		
		//returns the solid containing the grouped elements
		return new Solid(verticesArray, indicesArray, colorsArray);
	}
	
	/**
	 * Fills solid arrays with data about faces of an object generated whose status
	 * is as required
	 * 
	 * @param object3d solid object used to fill the arrays
	 * @param vertices vertices array to be filled
	 * @param indices indices array to be filled
	 * @param colors colors array to be filled
	 * @param faceStatus1 a status expected for the faces used to to fill the data arrays
	 * @param faceStatus2 a status expected for the faces used to to fill the data arrays
	 */
	private void groupObjectComponents(Object3D object, ArrayList vertices, ArrayList indices, ArrayList colors, int faceStatus1, int faceStatus2)
	{
		Face face;
		//for each face..
		for(int i=0;i<object.getNumFaces();i++)
		{
			face = object.getFace(i);
			//if the face status fits with the desired status...
			if(face.getStatus()==faceStatus1 || face.getStatus()==faceStatus2)
			{
				//adds the face elements into the arrays 
				Vertex[] faceVerts = {face.v1, face.v2, face.v3};
				for(int j=0;j<faceVerts.length;j++)
				{
					if(vertices.contains(faceVerts[j]))
					{
						indices.add(new Integer(vertices.indexOf(faceVerts[j])));
					}
					else
					{
						indices.add(new Integer(vertices.size()));
						vertices.add(faceVerts[j]);
						colors.add(faceVerts[j].getColor());
					}
				}
			}
		}
	}
}