import java.util.LinkedList;

class BFSsearchCellModel
{

	public BFSsearchCellModel()
	{

	}

	public int bfsSearch(CellModel currentCell, Robot superc)
	{
		for (int i = 0; i < currentCell.cells.size(); i++)
		{
			currentCell.cells.get(i).searched = false;
		}
		LinkedList<CellModel> currentBound = new LinkedList<>();
		currentCell.searched = true;
		switch (superc.thinkOrientation)
		{
			case 'N':
				if (forwardNorth(currentCell, currentBound) == 0)
					return 0;
				if (forwardWest(currentCell, currentBound) == 3)
					return 3;
				if (forwardSouth(currentCell, currentBound) == 2)
					return 2;
				if (forwardEast(currentCell, currentBound) == 1)
					return 1;
				break;
			case 'E':
				if (forwardEast(currentCell, currentBound) == 1)
					return 1;
				if (forwardNorth(currentCell, currentBound) == 0)
					return 0;
				if (forwardWest(currentCell, currentBound) == 3)
					return 3;
				if (forwardSouth(currentCell, currentBound) == 2)
					return 2;
				break;
			case 'S':
				if (forwardSouth(currentCell, currentBound) == 2)
					return 2;
				if (forwardEast(currentCell, currentBound) == 1)
					return 1;
				if (forwardNorth(currentCell, currentBound) == 0)
					return 0;
				if (forwardWest(currentCell, currentBound) == 3)
					return 3;
				break;
			case 'W':
				if (forwardWest(currentCell, currentBound) == 3)
					return 3;
				if (forwardSouth(currentCell, currentBound) == 2)
					return 2;
				if (forwardEast(currentCell, currentBound) == 1)
					return 1;
				if (forwardNorth(currentCell, currentBound) == 0)
					return 0;
				break;
		}
		while (currentBound.size() > 0)
		{
			currentCell = currentBound.removeFirst();
			if (currentCell.North != null)
			{
				if (!currentCell.North.blocking && currentCell.North.contents == 0)
				{
					if (!currentCell.North.searched)
					{
						if (!currentCell.North.visited)
						{
							return currentCell.movedFrom;
						}
						else
						{
							currentCell.North.movedFrom = currentCell.movedFrom;
							currentCell.North.searched = true;
							currentBound.add(currentCell.North);
						}
					}
				}
			}
			if (currentCell.East != null)
			{
				if (!currentCell.East.blocking && currentCell.East.contents == 0)
				{
					if (!currentCell.East.searched)
					{
						if (!currentCell.East.visited)
						{
							return currentCell.movedFrom;
						}
						else
						{
							currentCell.East.movedFrom = currentCell.movedFrom;
							currentCell.East.searched = true;
							currentBound.add(currentCell.East);
						}
					}
				}
			}
			if (currentCell.West != null)
			{
				if (!currentCell.West.blocking && currentCell.West.contents == 0)
				{
					if (!currentCell.West.searched)
					{
						if (!currentCell.West.visited)
						{
							return currentCell.movedFrom;
						}
						else
						{
							currentCell.West.movedFrom = currentCell.movedFrom;
							currentCell.West.searched = true;
							currentBound.add(currentCell.West);
						}
					}
				}
			}
			if (currentCell.South != null)
			{
				if (!currentCell.South.blocking && currentCell.South.contents == 0)
				{
					if (!currentCell.South.searched)
					{
						if (!currentCell.South.visited)
						{
							return currentCell.movedFrom;
						}
						else
						{
							currentCell.South.movedFrom = currentCell.movedFrom;
							currentCell.South.searched = true;
							currentBound.add(currentCell.South);
						}
					}
				}
			}
		}
		return -1;
	}

	private int forwardNorth(CellModel currentCell, LinkedList<CellModel> currentBound)
	{
		if (currentCell.North != null)
		{
			if (!currentCell.North.blocking && currentCell.North.contents == 0)
			{
				if (!currentCell.North.visited)
				{
					return 0;
				}
				else
				{
					currentCell.North.searched = true;
					currentCell.North.movedFrom = 0;
					currentBound.add(currentCell.North);
				}
			}
		}
		return -1;
	}

	private int forwardEast(CellModel currentCell, LinkedList<CellModel> currentBound)
	{
		if (currentCell.East != null)
		{
			if (!currentCell.East.blocking && currentCell.East.contents == 0)
			{
				if (!currentCell.East.visited)
				{
					return 1;
				}
				else
				{
					currentCell.East.searched = true;
					currentCell.East.movedFrom = 1;
					currentBound.add(currentCell.East);
				}
			}
		}
		return -1;
	}

	private int forwardWest(CellModel currentCell, LinkedList<CellModel> currentBound)
	{
		if (currentCell.West != null)
		{
			if (!currentCell.West.blocking && currentCell.West.contents == 0)
			{
				if (!currentCell.West.visited)
				{
					return 3;
				}
				else
				{
					currentCell.West.searched = true;
					currentCell.West.movedFrom = 3;
					currentBound.add(currentCell.West);
				}
			}
		}
		return -1;
	}

	private int forwardSouth(CellModel currentCell, LinkedList<CellModel> currentBound)
	{
		if (currentCell.South != null)
		{
			if (!currentCell.South.blocking && currentCell.South.contents == 0)
			{
				if (!currentCell.South.visited)
				{
					return 2;
				}
				else
				{
					currentCell.South.searched = true;
					currentCell.South.movedFrom = 2;
					currentBound.add(currentCell.South);
				}
			}
		}
		return -1;
	}

	public void removeBlockers(CellModel currentCell)
	{
		for (int i = 0; i < currentCell.cells.size(); i++)
		{
			currentCell.cells.get(i).blocking = false;
		}
	}

}