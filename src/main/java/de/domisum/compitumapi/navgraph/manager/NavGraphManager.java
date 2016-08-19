package de.domisum.compitumapi.navgraph.manager;

import de.domisum.auxiliumapi.util.FileUtil;
import de.domisum.auxiliumapi.util.java.GsonUtil;
import de.domisum.compitumapi.CompitumAPI;
import de.domisum.compitumapi.navgraph.NavGraph;
import de.domisum.compitumapi.navgraph.edit.NavGraphEditManager;
import de.domisum.compitumapi.navgraph.json.SerializationGraph;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class NavGraphManager
{

	// CONSTANTS
	private static final String NAV_GRAPHS_DIRECTORY = "navGraphs";
	private static final String NAV_GRAPH_FILE_EXTENSION = ".navGraph.json";

	// REFERENCES
	private Set<NavGraph> graphs = new HashSet<>();

	private NavGraphEditManager editManager;


	// -------
	// CONSTRUCTOR
	// -------
	public NavGraphManager()
	{

	}

	public void initiialize()
	{
		loadGraphs();

		this.editManager = new NavGraphEditManager();
		this.editManager.initialize();
	}

	public void terminate()
	{
		this.editManager.terminate();

		saveGraphs();
	}


	// -------
	// LOADING
	// -------
	private void loadGraphs()
	{
		CompitumAPI.getLogger().info("Loading NavGraphs...");

		File baseDir = new File(NAV_GRAPHS_DIRECTORY);
		// noinspection ResultOfMethodCallIgnored
		baseDir.mkdirs();

		for(File file : FileUtil.listFilesRecursively(baseDir))
		{
			if(file.isDirectory())
				continue;

			if(!file.getName().endsWith(NAV_GRAPH_FILE_EXTENSION))
			{
				CompitumAPI.getLogger().warning(
						"The file '"+file.getAbsolutePath()+" in the navGraph directory was skipped since it doesn't end with '"
								+NAV_GRAPH_FILE_EXTENSION+"'.");
				continue;
			}

			String graphId = FileUtil.getIdentifier(baseDir, file, NAV_GRAPH_FILE_EXTENSION);

			String content = FileUtil.readFileToString(file);
			NavGraph graph;
			try
			{
				SerializationGraph serializationGraph = GsonUtil.get().fromJson(content, SerializationGraph.class);
				graph = serializationGraph.convertToNavGraph(graphId);
				this.graphs.add(graph);

				CompitumAPI.getLogger().info("Loaded NavGraph '"+graph.getId()+"'");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		CompitumAPI.getLogger().info("Loading NavGraphs complete: loaded "+this.graphs.size()+" NavGraph(s)");
	}


	// -------
	// SAVING
	// -------
	public void additionalSave()
	{
		saveGraphs();

		for(Player p : Bukkit.getOnlinePlayers())
			p.sendMessage("Saved graph data to disk.");
	}

	private void saveGraphs()
	{
		CompitumAPI.getLogger().info("Saving NavGraphs...");

		File baseDir = new File(NAV_GRAPHS_DIRECTORY);
		// noinspection ResultOfMethodCallIgnored
		baseDir.mkdirs();
		// disable this since deleting a navgraph is quite a major thing to do, so don't risk losing them
		// FileUtil.deleteDirectoryContents(baseDir);

		for(NavGraph graph : this.graphs)
		{
			File file = new File(baseDir, graph.getId()+NAV_GRAPH_FILE_EXTENSION);

			try
			{
				SerializationGraph sGraph = new SerializationGraph(graph);
				String json = GsonUtil.getPretty().toJson(sGraph);

				FileUtil.writeStringToFile(file, json);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		CompitumAPI.getLogger().info("Saving NavGraphs complete: saved "+this.graphs.size()+" NavGraph(s)");
	}


	// -------
	// GETTERS
	// -------
	public NavGraphEditManager getEditManager()
	{
		return this.editManager;
	}

	public NavGraph getGraphAt(Location location)
	{
		for(NavGraph g : this.graphs)
			if(g.isInRange(location))
				return g;

		return null;
	}


}
