import java.util.LinkedList;

class CellModellead
{
	CellModellead North;
	CellModellead East;
	CellModellead South;
	CellModellead West;
	boolean visited = false;
	boolean blocking = false;
	boolean searched = false;
	int movedFrom;
	final int xCord;
	final int yCord;
	int contents = 0;
	private int observedBlock = 0;
	final LinkedList<CellModellead> cells;
	private final LeaderSub superc;

	public CellModellead(int xCord, int yCord, LinkedList<CellModellead> cells, LeaderSub superc)
	{
		this.cells = cells;
		this.xCord = xCord;
		this.yCord = yCord;
		this.superc = superc;
		cells.add(this);
	}

	public void setNorth(int distance)
	{
		if (distance > 0)
		{
			if (North == null)
			{
				CellModellead cellLook = null;
				for (CellModellead cell : cells)
				{
					if (cell.equals(xCord - 1, yCord))
					{
						cellLook = cell;
						break;
					}
				}
				if (cellLook == null)
				{
					cellLook = new CellModellead(xCord - 1, yCord, cells, superc);
				}
				North = cellLook;
				cellLook.South = this;
			}
			North.contents = 0;
			North.observedBlock = 0;
			North.blocking=false;
			North.setNorth(distance - 1);
		} else
		{
			CellModellead cellLook = null;
			for (CellModellead cell : cells)
			{
				if (cell.equals(xCord - 1, yCord))
				{
					cellLook = cell;
					break;
				}
			}
			if (cellLook == null)
			{
				cellLook = new CellModellead(xCord - 1, yCord, cells, superc);
				cellLook.contents = 1;
			}
			North = cellLook;
			cellLook.South = this;
		}
	}

	public void setEast(int distance)
	{
		if (distance > 0)
		{
			if (East == null)
			{
				CellModellead cellLook = null;
				for (CellModellead cell : cells)
				{
					if (cell.equals(xCord, yCord + 1))
					{
						cellLook = cell;
						break;
					}
				}
				if (cellLook == null)
				{
					cellLook = new CellModellead(xCord, yCord + 1, cells, superc);
				}
				East = cellLook;
				cellLook.West = this;
			}
			East.blocking=false;
			East.contents = 0;
			East.observedBlock = 0;
			East.setEast(distance - 1);
		} else
		{
			CellModellead cellLook = null;
			for (CellModellead cell : cells)
			{
				if (cell.equals(xCord, yCord + 1))
				{
					cellLook = cell;
					break;
				}
			}
			if (cellLook == null)
			{
				cellLook = new CellModellead(xCord, yCord + 1, cells, superc);
				cellLook.contents = 1;
			}
			East = cellLook;
			cellLook.West = this;
		}
	}

	public void setSouth(int distance)
	{
		if (distance > 0)
		{
			if (South == null)
			{
				CellModellead cellLook = null;
				for (CellModellead cell : cells)
				{
					if (cell.equals(xCord + 1, yCord))
					{
						cellLook = cell;
						break;
					}
				}
				if (cellLook == null)
				{
					cellLook = new CellModellead(xCord + 1, yCord, cells, superc);
				}
				South = cellLook;
				cellLook.North = this;
			}
			South.blocking=false;
			South.contents = 0;
			South.observedBlock = 0;
			South.setSouth(distance - 1);
		} else
		{
			CellModellead cellLook = null;
			for (CellModellead cell : cells)
			{
				if (cell.equals(xCord + 1, yCord))
				{
					cellLook = cell;
					break;
				}
			}
			if (cellLook == null)
			{
				cellLook = new CellModellead(xCord + 1, yCord, cells, superc);
				cellLook.contents = 1;
			}
			South = cellLook;
			cellLook.North = this;
		}
	}

	public void setWest(int distance)
	{
		if (distance > 0)
		{
			if (West == null)
			{
				CellModellead cellLook = null;
				for (CellModellead cell : cells)
				{
					if (cell.equals(xCord, yCord - 1))
					{
						cellLook = cell;
						break;
					}
				}
				if (cellLook == null)
				{
					cellLook = new CellModellead(xCord, yCord - 1, cells, superc);
				}
				West = cellLook;
				cellLook.East = this;
			}
			West.blocking=false;
			West.contents = 0;
			West.observedBlock = 0;
			West.setWest(distance - 1);
		} else
		{
			CellModellead cellLook = null;
			for (CellModellead cell : cells)
			{
				if (cell.equals(xCord, yCord - 1))
				{
					cellLook = cell;
					break;
				}
			}
			if (cellLook == null)
			{
				cellLook = new CellModellead(xCord, yCord - 1, cells, superc);
				cellLook.contents = 1;
			}
			West = cellLook;
			cellLook.East = this;
		}
	}

	private boolean equals(int xCordL, int yCordL)
	{
        return xCordL == xCord && yCordL == yCord;
	}

	public String toString()
	{
		return "[" + xCord + "," + yCord + "," + contents + ","+ observedBlock+","+visited+"]";
	}

}
