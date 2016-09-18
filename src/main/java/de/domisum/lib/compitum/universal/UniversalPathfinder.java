package de.domisum.lib.compitum.universal;


import de.domisum.lib.auxilium.util.java.annotations.APIUsage;
import de.domisum.lib.compitum.CompitumLib;
import de.domisum.lib.compitum.navmesh.NavMesh;
import de.domisum.lib.compitum.navmesh.NavMeshManager;
import de.domisum.lib.compitum.navmesh.path.NavMeshTrianglePathfinder;
import de.domisum.lib.compitum.transitionalpath.path.TransitionalBlockPath;
import de.domisum.lib.compitum.transitionalpath.path.TransitionalPath;
import de.domisum.lib.compitum.transitionalpath.path.TransitionalPathSmoother;
import de.domisum.lib.compitum.transitionalpath.pathfinders.TransitionalAStar;
import org.bukkit.Location;

@APIUsage
public class UniversalPathfinder
{

	// INPUT
	private Location start;
	private Location target;

	// PROPERTIES


	// OUTPUT
	private TransitionalPath path;

	private String diagnose;
	private String error;


	// -------
	// CONSTRUCTOR
	// -------
	@APIUsage
	public UniversalPathfinder(Location start, Location target)
	{
		this.start = fixPathfindingLocation(start);
		this.target = fixPathfindingLocation(target);
	}


	// -------
	// GETTERS
	// -------
	@APIUsage
	public TransitionalPath getPath()
	{
		return this.path;
	}

	@APIUsage
	public boolean isPathFound()
	{
		return this.path != null;
	}


	@APIUsage
	public String getDiagnose()
	{
		return this.diagnose;
	}

	@APIUsage
	public String getError()
	{
		return this.error;
	}


	// -------
	// PATHFINDING
	// -------
	@APIUsage
	public void findPath()
	{
		navMeshCheck:
		if(CompitumLib.areNavMeshesEnabled())
		{
			NavMeshManager nmm = CompitumLib.getNavMeshManager();
			NavMesh meshAtStart = nmm.getNavMeshAt(this.start);
			if(meshAtStart == null)
				break navMeshCheck;

			NavMesh meshAtTarget = nmm.getNavMeshAt(this.target);
			if(meshAtTarget == null)
				break navMeshCheck;

			if(meshAtStart != meshAtTarget)
				break navMeshCheck;

			useNavMesh(meshAtStart);
			return;
		}

		useWorldAStar();
	}


	private void useWorldAStar()
	{
		TransitionalAStar pathfinder = new TransitionalAStar(this.start, this.target);
		pathfinder.findPath();
		this.diagnose = pathfinder.getDiagnose();

		if(!pathfinder.pathFound())
		{
			this.error = pathfinder.getError();
			return;
		}
		TransitionalBlockPath blockPath = pathfinder.getPath();

		TransitionalPathSmoother smoother = new TransitionalPathSmoother(blockPath);
		smoother.convert();
		this.path = smoother.getSmoothPath();
	}

	private void useNavMesh(NavMesh navMesh)
	{
		NavMeshTrianglePathfinder pathfinder = new NavMeshTrianglePathfinder(this.start, this.target, navMesh);
		pathfinder.findPath();
		this.diagnose = pathfinder.getDiagnose();
		if(!pathfinder.pathFound())
		{
			this.error = pathfinder.getError();
			return;
		}

		this.path = pathfinder.getPath();
	}


	@APIUsage
	public static Location fixPathfindingLocation(Location location)
	{
		location.setY(Math.floor(location.getY()));

		String materialName = location.getBlock().getType().name();
		if(materialName.contains("SLAB") || materialName.contains("STEP"))
			location = location.add(0, 1, 0);

		return location;
	}

}