import lejos.hardware.BrickFinder;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;

import java.util.LinkedList;
import java.util.Random;

class Robot implements Runnable {
	private final int port;
	private final String ip;
	public com23 inOut;
	public int ID;
	private Cell[][] map;
	private Cell[][] map3;
	private Cell[][] map5;
	private Cell[][] map8;
	char orientation = 'N';
	char thinkOrientation = 'N';
	public Movement mover;
	public Scanner scanner;
	private CellModel currentCell = new CellModel(0, 0, new LinkedList<CellModel>(), this);

	public Robot(String ip, int port) {
		this.port = port;
		this.ip = ip;
	}

	public void run() {
		try {
			if (Config.Simulated) {
				mover = new MovementSimulated(this);
				scanner = new ScannerSimulated(this);
			} else {
				mover = new MovementPhysical(this, SensorPort.S1, SensorPort.S4, SensorPort.S2, Motor.A, Motor.D);
				scanner = new ScannerPhysical(this, SensorPort.S3, Motor.B);
				EV3 ev3 = (EV3) BrickFinder.getLocal();
				TextLCD lcd = ev3.getTextLCD();
				lcd.drawString("I'm Follow", 2, 3);
			}
			System.out.println("connected");
			inOut = new com23(ip, port);
			ID = inOut.readInt("Reading my ID");
			orientation = inOut.readChar("reading orientation");
			map = (Cell[][]) inOut.readObj("Reading Map");
			map3 = new Cell[map.length][map.length];
			map5 = new Cell[map.length][map.length];
			map8 = new Cell[map.length][map.length];
			for (Cell[] aMap : map) {
				for (int j = 0; j < map.length; j++) {
					if (aMap[j].contents != 0 && aMap[j].contents != 1) {
						aMap[j].contents = 0;
					}
					if (aMap[j].contents == 0) {
						aMap[j].chanceOfSelf[ID - 2][0]++;
						aMap[j].chanceOfSelf[ID - 2][1]++;
						aMap[j].chanceOfSelf[ID - 2][2]++;
						aMap[j].chanceOfSelf[ID - 2][3]++;
					}
				}
			}
			for (int i = 0; i < map.length; i++) {
				for (int j = 0; j < map.length; j++) {
					map3[i][j] = map[map.length - 1 - i][map.length - j - 1];
				}
			}
			for (int i = 0; i < map.length; i++) {
				for (int j = 0; j < map.length; j++) {
					map5[i][j] = map[map.length - j - 1][i];
				}
			}

			for (int i = 0; i < map.length; i++) {
				for (int j = 0; j < map.length; j++) {
					map8[i][j] = map5[map.length - 1 - i][map.length - j - 1];
				}
			}
			Cell[][][] groupOfMaps = new Cell[4][map.length][map.length];
			groupOfMaps[0] = map;
			groupOfMaps[1] = map3;
			groupOfMaps[2] = map5;
			groupOfMaps[3] = map8;
			inOut.readString("starting");
			inOut.readString("firstTurn");
			BFSsearchCellModel searcher = new BFSsearchCellModel();
			Random rand = new Random();
			int blockersRemoved = 0;
			mover.resetGyroTacho();
			while (true) {
				inOut.sendString("exit?", "exit?");
				if (inOut.readBool("should I exit?"))
					break;
				particleFilterDirection();
				currentCell.visited = true;
				int distance = 0;
				LinkedList<Integer> moves = new LinkedList<>();
				for (int i = 0; i < 4; i++) {
					moves.add(i);
				}
				while (distance == 0) {
					int moveToTake = searcher.bfsSearch(currentCell, this);
					if (moveToTake == -1) {
						searcher.removeBlockers(currentCell);
						blockersRemoved++;
						moveToTake = searcher.bfsSearch(currentCell, this);
						if (moveToTake == -1) {
							if (moves.size() == 0) {
								moves = new LinkedList<>();
								for (int i = 0; i < 4; i++) {
									moves.add(i);
								}
							}
							int randomNum = rand.nextInt(moves.size());
							moveToTake = moves.remove(randomNum);
						}
					}
					if (blockersRemoved > 4) {
						currentCell = new CellModel(0, 0, new LinkedList<CellModel>(), this);
						currentCell.visited = true;
						particleFilterDirection();
						moveToTake = searcher.bfsSearch(currentCell, this);
						blockersRemoved = 0;
						if (moveToTake == -1) {
							inOut.sendString("whereIThink", "thisIsWhereIThink");
							inOut.sendObject(map);
							inOut.sendString("finishedEndScanning", "finished scanning");
							scanner.scanDetect();
							inOut.readString("waiting for next ");
							break;
						}
					}
					switch (moveToTake) {
					case 0:
						mover.faceNorthBefore();
						break;
					case 1:
						mover.faceEastBefore();
						break;
					case 2:
						mover.faceSouthBefore();
						break;
					case 3:
						mover.faceWestBefore();
						break;
					}
					distance = scanner.scanForwardDistance(false);
					if (distance == 0) {
						switch (thinkOrientation) {
						case 'N':
							currentCell.North.blocking = true;
							break;
						case 'E':
							currentCell.East.blocking = true;
							break;
						case 'S':
							currentCell.South.blocking = true;
							break;
						case 'W':
							currentCell.West.blocking = true;
							break;
						}
					} else {
						mover.moveForward();
						moveForwardPart(map, 0);
						moveForwardPart(map3, 1);
						moveForwardPart(map5, 2);
						moveForwardPart(map8, 3);
						switch (thinkOrientation) {
						case 'N':
							currentCell = currentCell.North;
							break;
						case 'E':
							currentCell = currentCell.East;
							break;
						case 'S':
							currentCell = currentCell.South;
							break;
						case 'W':
							currentCell = currentCell.West;
							break;
						}
						recalulate(map);
						inOut.sendString("whereIThink", "thisIsWhereIThink");
						inOut.sendObject(map);
						inOut.sendString("finishedEndScanning", "finished scanning");
						scanner.scanDetect();
						inOut.readString("waiting for next ");
					}
				}
			}
			inOut.cleanUp();
		} catch (Exception l) {
			System.out.println("failed");
			l.printStackTrace();
		}
	}

	private void particleFilterDirection() throws Exception {
		int distance;
		for (Cell[] aMap : map) {
			for (int j = 0; j < map.length; j++) {
				aMap[j].chanceOfTemp[ID - 2][0] = 1;
				aMap[j].chanceOfTemp[ID - 2][1] = 1;
				aMap[j].chanceOfTemp[ID - 2][2] = 1;
				aMap[j].chanceOfTemp[ID - 2][3] = 1;
			}
		}
		inOut.sendString("readMap", "reading map");
		Cell[][] tempMap = (Cell[][]) inOut.readObj("reading map");
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map.length; j++) {
				map[i][j].chanceOfOccupancy = tempMap[i][j].chanceOfOccupancy;
				map[i][j].sharedCell = tempMap[i][j].sharedCell;
			}
		}
		switch (thinkOrientation) {
		case 'N':
			distance = particleFilter(-1, 0);
			currentCell.setNorth(distance);
			mover.turnRight();
			distance = particleFilter(0, 1);
			currentCell.setEast(distance);
			mover.turnRight();
			distance = particleFilter(1, 0);
			currentCell.setSouth(distance);
			mover.turnRight();
			distance = particleFilter(0, -1);
			currentCell.setWest(distance);
			break;
		case 'E':
			distance = particleFilter(0, 1);
			currentCell.setEast(distance);
			mover.turnRight();
			distance = particleFilter(1, 0);
			currentCell.setSouth(distance);
			mover.turnRight();
			distance = particleFilter(0, -1);
			currentCell.setWest(distance);
			mover.turnRight();
			distance = particleFilter(-1, 0);
			currentCell.setNorth(distance);
			break;
		case 'S':
			distance = particleFilter(1, 0);
			currentCell.setSouth(distance);
			mover.turnRight();
			distance = particleFilter(0, -1);
			currentCell.setWest(distance);
			mover.turnRight();
			distance = particleFilter(-1, 0);
			currentCell.setNorth(distance);
			mover.turnRight();
			distance = particleFilter(0, 1);
			currentCell.setEast(distance);
			break;
		case 'W':
			distance = particleFilter(0, -1);
			currentCell.setWest(distance);
			mover.turnRight();
			distance = particleFilter(-1, 0);
			currentCell.setNorth(distance);
			mover.turnRight();
			distance = particleFilter(0, 1);
			currentCell.setEast(distance);
			mover.turnRight();
			distance = particleFilter(1, 0);
			currentCell.setSouth(distance);
			break;
		}
		mover.turnRight();
		recalulatePercent(map);
	}

	private int particleFilter(int xReduction, int yReduction) throws Exception {
		int distance;

		distance = scanner.scanForwardDistance(false);
		int[] amountFound = new int[11];
		amountFound[distance]++;
		for (int i = 0; i < 4; i++) {
			amountFound[scanner.scanForwardDistance(false)]++;
		}
		int maxValue = 0;
		int maxIndex = 0;
		for (int i = 0; i < amountFound.length; i++) {
			if (maxValue < amountFound[i]) {
				maxValue = amountFound[i];
				maxIndex = i;
			}
		}
		distance = maxIndex;
		updateSpecificMapPart(map, distance, xReduction, yReduction, 0);
		updateSpecificMapPart(map3, distance, xReduction, yReduction, 1);
		updateSpecificMapPart(map5, distance, xReduction, yReduction, 2);
		updateSpecificMapPart(map8, distance, xReduction, yReduction, 3);
		return distance;
	}

	private void updateSpecificMapPart(Cell[][] mapHere, int distance, int xReduction, int yReduction, int mapValue) {
		for (int i = 0; i < mapHere.length; i++) {
			for (int j = 0; j < mapHere[0].length; j++) {
				if (mapHere[i][j].contents == 0) {
					boolean valid = true;
					for (int k = 0; k < distance; k++) {
						int xValue = i + (k * xReduction) + xReduction;
						int yValue = j + (k * yReduction) + yReduction;
						if (mapHere[xValue][yValue].contents == 1||(mapHere[xValue][yValue].chanceOfOccupancy[mapValue]>0.9&&mapHere[xValue][yValue].chanceOfOccupancy[mapValue]<1)) {
							valid = false;
							if (Config.Simulated)
								mapHere[i][j].chanceOfTemp[ID - 2][mapValue] = 0.2;
							else
								mapHere[i][j].chanceOfTemp[ID - 2][mapValue] *= 0.2;
							break;
						}
					}
					if (valid) {
						int xValue = i + (distance * xReduction) + xReduction;
						int yValue = j + (distance * yReduction) + yReduction;
						if (!(xValue >= mapHere.length || xValue < 0 || yValue >= mapHere[0].length || yValue < 0)) {
							int howMany = 0;
							double chance = 0;
							for (int s = 0; s < mapHere[xValue][yValue].chanceOfOccupancy.length; s++) {
								if (mapHere[xValue][yValue].chanceOfOccupancy[s] > 0.4) {
									if (chance < mapHere[xValue][yValue].chanceOfOccupancy[s]) {
										chance = mapHere[xValue][yValue].chanceOfOccupancy[s];
									}
									howMany++;
								}
							}
							double largestSymmetric = 0;
							boolean sharedCross = false;
							for (int s = 0; s < mapHere[xValue][yValue].chanceOfOccupancy.length; s++) {
								if (largestSymmetric < map[xValue][yValue].chanceOfOccupancy[s]) {
									largestSymmetric = map[xValue][yValue].chanceOfOccupancy[s];
								}
								if (largestSymmetric < map3[xValue][yValue].chanceOfOccupancy[s]) {
									largestSymmetric = map3[xValue][yValue].chanceOfOccupancy[s];
								}
								if (largestSymmetric < map5[xValue][yValue].chanceOfOccupancy[s]) {
									largestSymmetric = map5[xValue][yValue].chanceOfOccupancy[s];
								}
								if (largestSymmetric < map8[xValue][yValue].chanceOfOccupancy[s]) {
									largestSymmetric = map8[xValue][yValue].chanceOfOccupancy[s];
								}
								if (map[xValue][yValue].sharedCell[s]) {
									sharedCross = true;
								}
								if (map3[xValue][yValue].sharedCell[s]) {
									sharedCross = true;
								}
								if (map5[xValue][yValue].sharedCell[s]) {
									sharedCross = true;
								}
								if (map8[xValue][yValue].sharedCell[s]) {
									sharedCross = true;
								}
							}
							if (mapHere[xValue][yValue].contents != 1 && howMany < 2 && (largestSymmetric > 0.80)
									&& !sharedCross) {
								if (chance > 0.9)
									mapHere[i][j].chanceOfTemp[ID - 2][mapValue] *= 1;
								else {
									if (Config.Simulated)
										mapHere[i][j].chanceOfTemp[ID - 2][mapValue] *= 0.1;
									else {
										mapHere[i][j].chanceOfTemp[ID - 2][mapValue] *= 0.1;
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private void moveForwardPart(Cell[][] mapHere, int mapValue) {
		switch (thinkOrientation) {
		case 'N':
			for (int i = 0; i < mapHere.length; i++) {
				for (int j = 0; j < mapHere[0].length; j++) {
					if (mapHere[i][j].contents == 0) {
						mapHere[i][j].chanceOfSelfPercent[ID - 2][mapValue] = mapHere[i + 1][j].chanceOfSelfPercent[ID
								- 2][mapValue];
					}
				}
			}
			break;
		case 'E':
			for (Cell[] aMapHere : mapHere) {
				for (int j = mapHere[0].length - 1; j >= 0; j--) {
					if (aMapHere[j].contents == 0) {
						aMapHere[j].chanceOfSelfPercent[ID - 2][mapValue] = aMapHere[j - 1].chanceOfSelfPercent[ID
								- 2][mapValue];
					}
				}
			}
			break;
		case 'S':
			for (int i = mapHere.length - 1; i >= 0; i--) {
				for (int j = 0; j < mapHere[0].length; j++) {
					if (mapHere[i][j].contents == 0) {
						mapHere[i][j].chanceOfSelfPercent[ID - 2][mapValue] = mapHere[i - 1][j].chanceOfSelfPercent[ID
								- 2][mapValue];
					}
				}
			}
			break;
		case 'W':
			for (Cell[] aMapHere : mapHere) {
				for (int j = 0; j < mapHere[0].length; j++) {
					if (aMapHere[j].contents == 0) {
						aMapHere[j].chanceOfSelfPercent[ID - 2][mapValue] = aMapHere[j + 1].chanceOfSelfPercent[ID
								- 2][mapValue];
					}
				}
			}
			break;
		}
	}

	private void recalulate(Cell[][] map2) {
		double sum2 = 0;
		for (int i = 0; i < map2.length; i++) {
			for (int j = 0; j < map2.length; j++) {
				for (int k = 0; k < map[i][j].chanceOfTemp.length; k++) {
					sum2 += map2[i][j].chanceOfSelfPercent[ID - 2][k];
				}
			}
		}
		for (int i = 0; i < map2.length; i++) {
			for (int j = 0; j < map2.length; j++) {
				for (int k = 0; k < map[i][j].chanceOfTemp.length; k++) {
					map2[i][j].chanceOfSelfPercent[ID - 2][k] = map2[i][j].chanceOfSelfPercent[ID - 2][k] / sum2;
				}
			}
		}
	}

	private void recalulatePercent(Cell[][] map2) {
		double sum = 0;
		for (Cell[] aMap : map) {
			for (int j = 0; j < map.length; j++) {
				for (int k = 0; k < aMap[j].chanceOfSelf.length; k++) {
					sum += aMap[j].chanceOfTemp[ID - 2][k];
				}
			}
		}
		if (Double.isNaN(sum) || sum == 0) {
			for (Cell[] aMap : map) {
				for (int j = 0; j < map.length; j++) {
					if (aMap[j].contents == 0) {
						aMap[j].chanceOfTemp[ID - 2][0] += 0.0001;
						aMap[j].chanceOfTemp[ID - 2][1] += 0.0001;
						aMap[j].chanceOfTemp[ID - 2][2] += 0.0001;
						aMap[j].chanceOfTemp[ID - 2][3] += 0.0001;
						aMap[j].chanceOfSelfPercent[ID - 2][0] += 0.0001;
						aMap[j].chanceOfSelfPercent[ID - 2][1] += 0.0001;
						aMap[j].chanceOfSelfPercent[ID - 2][2] += 0.0001;
						aMap[j].chanceOfSelfPercent[ID - 2][3] += 0.0001;
					}
				}
			}
			sum = 1;
		}
		for (int i = 0; i < map2.length; i++) {
			for (int j = 0; j < map2.length; j++) {
				for (int k = 0; k < map[i][j].chanceOfTemp.length; k++) {
					map2[i][j].chanceOfTemp[ID - 2][k] = map2[i][j].chanceOfTemp[ID - 2][k] / sum;
				}
			}
		}
		double sum2 = 0;
		for (int i = 0; i < map2.length; i++) {
			for (int j = 0; j < map2.length; j++) {
				for (int k = 0; k < map[i][j].chanceOfTemp.length; k++) {
					map2[i][j].chanceOfSelfPercent[ID - 2][k] = (map2[i][j].chanceOfSelfPercent[ID - 2][k]
							* map2[i][j].chanceOfTemp[ID - 2][k]);
					sum2 += map2[i][j].chanceOfSelfPercent[ID - 2][k];
				}
			}
		}
		if (Double.isNaN(sum2) || sum2 == 0) {
			for (Cell[] aMap : map) {
				for (int j = 0; j < map.length; j++) {
					if (aMap[j].contents == 0) {
						aMap[j].chanceOfTemp[ID - 2][0] += 0.0001;
						aMap[j].chanceOfTemp[ID - 2][1] += 0.0001;
						aMap[j].chanceOfTemp[ID - 2][2] += 0.0001;
						aMap[j].chanceOfTemp[ID - 2][3] += 0.0001;
						aMap[j].chanceOfSelfPercent[ID - 2][0] += 0.0001;
						aMap[j].chanceOfSelfPercent[ID - 2][1] += 0.0001;
						aMap[j].chanceOfSelfPercent[ID - 2][2] += 0.0001;
						aMap[j].chanceOfSelfPercent[ID - 2][3] += 0.0001;
					}
				}
			}
			sum2 = 1;
		}
		for (int i = 0; i < map2.length; i++) {
			for (int j = 0; j < map2.length; j++) {
				for (int k = 0; k < map[i][j].chanceOfTemp.length; k++) {
					map2[i][j].chanceOfSelfPercent[ID - 2][k] = (map2[i][j].chanceOfSelfPercent[ID - 2][k] / sum2);
				}
			}
		}
	}
}
