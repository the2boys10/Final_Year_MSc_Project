import lejos.hardware.BrickFinder;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;

import java.util.LinkedList;
import java.util.Random;

class LeaderSub implements Runnable {
	private final int port;
	private final String ip;
	public Comlead inOut;
	public int ID;
	private Cell[][] map;
	char orientation = 'N';
	char thinkOrientation = 'N';
	public Movementlead mover;
	public Scannerlead scanner;
	private CellModellead currentCell = new CellModellead(0, 0, new LinkedList<CellModellead>(), this);

	public LeaderSub(String ip, int port) {
		this.port = port;
		this.ip = ip;
	}

	public void run() {
		try {
			if (Config.Simulated) {
				mover = new MovementSimulatedlead(this);
				scanner = new ScannerSimulatedlead(this);
			} else {
				mover = new MovementPhysical(this, SensorPort.S1, SensorPort.S4, SensorPort.S2, Motor.A, Motor.D);
				scanner = new ScannerPhysical(this, SensorPort.S3, Motor.B);
				EV3 ev3 = (EV3) BrickFinder.getLocal();
				TextLCD lcd = ev3.getTextLCD();
				lcd.drawString("I'm Lead", 2, 3);
			}
			System.out.print("connected");
			inOut = new Comlead(ip, port, this);
			ID = inOut.readInt("Reading my ID");
			inOut.readChar("reading orientation");
			map = (Cell[][]) inOut.readObj("Reading Map");
			for (Cell[] aMap2 : map) {
				for (int j = 0; j < map.length; j++) {
					if (aMap2[j].contents != 0 && aMap2[j].contents != 1) {
						aMap2[j].contents = 0;
					}
					if (aMap2[j].contents == 0) {
						aMap2[j].chanceOfSelf[ID - 2][1] = 0;
						aMap2[j].chanceOfSelf[ID - 2][2] = 0;
						aMap2[j].chanceOfSelf[ID - 2][3] = 0;
						aMap2[j].chanceOfSelfPercent[ID - 2][1] = 0;
						aMap2[j].chanceOfSelfPercent[ID - 2][2] = 0;
						aMap2[j].chanceOfSelfPercent[ID - 2][3] = 0;
					}
				}
			}
			inOut.readString("starting");
			inOut.readString("firstTurn");
			BFSsearchCellModellead searcher = new BFSsearchCellModellead();
			Random rand = new Random();
			int blockersRemoved = 0;
			while (true) {
				inOut.sendString("exit?", "exit?");
				if (inOut.readBool("should I exit?"))
					break;
				particleFilterDirection();
				int distance = 0;
				LinkedList<Integer> moves = new LinkedList<Integer>();
				for (int i = 0; i < 4; i++) {
					moves.add(i);
				}
				double maxVal = 0;
				for (Cell[] aMap1 : map) {
					for (int j = 0; j < map.length; j++) {
						for (int k = 0; k < aMap1[j].chanceOfSelf[ID - 2].length; k++)
							if (aMap1[j].chanceOfSelf[ID - 2][k] > maxVal) {
								maxVal = aMap1[j].chanceOfSelf[ID - 2][k];
							}
					}
				}
				if (rand.nextInt(100) > 90 && maxVal > 0.90) {
					inOut.sendString("whereIThink", "thisIsWhereIThink");
					inOut.sendObject(map);
					inOut.sendString("finishedEndScanning", "finished scanning");
					inOut.readString("waiting for next ");
				} else
					while (distance == 0) {
						int moveToTake;
						moveToTake = searcher.bfsSearch(currentCell, this);
						if (moveToTake == -1) {
							searcher.removeBlockers(currentCell);
							blockersRemoved++;
							moveToTake = searcher.bfsSearch(currentCell, this);
							if (moveToTake == -1) {
								if (moves.size() == 0) {
									moves = new LinkedList<Integer>();
									for (int i = 0; i < 4; i++) {
										moves.add(i);
									}
								}
								int randomNum = rand.nextInt(moves.size());
								moveToTake = moves.remove(randomNum);
							}
						}
						if (blockersRemoved > 4) {
							currentCell = new CellModellead(0, 0, new LinkedList<CellModellead>(), this);
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
							switch (thinkOrientation) {
							case 'N':
								for (int i = 0; i < map.length; i++) {
									for (int j = 0; j < map[0].length; j++) {
										if (map[i][j].contents == 0) {
											map[i][j].chanceOfSelfPercent[ID
													- 2][0] = map[i + 1][j].chanceOfSelfPercent[ID - 2][0];
										}
									}
								}
								currentCell = currentCell.North;
								break;
							case 'E':
								for (Cell[] aMap : map) {
									for (int j = map[0].length - 1; j >= 0; j--) {
										if (aMap[j].contents == 0) {
											aMap[j].chanceOfSelfPercent[ID - 2][0] = aMap[j - 1].chanceOfSelfPercent[ID
													- 2][0];
										}
									}
								}
								currentCell = currentCell.East;
								break;
							case 'S':
								for (int i = map.length - 1; i >= 0; i--) {
									for (int j = 0; j < map[0].length; j++) {
										if (map[i][j].contents == 0) {
											map[i][j].chanceOfSelfPercent[ID
													- 2][0] = map[i - 1][j].chanceOfSelfPercent[ID - 2][0];
										}
									}
								}
								currentCell = currentCell.South;
								break;
							case 'W':
								for (Cell[] aMap : map) {
									for (int j = 0; j < map[0].length; j++) {
										if (aMap[j].contents == 0) {
											aMap[j].chanceOfSelfPercent[ID - 2][0] = aMap[j + 1].chanceOfSelfPercent[ID
													- 2][0];
										}
									}
								}
								currentCell = currentCell.West;
								break;
							}
							recalulate(map);
							inOut.sendString("whereIThink", "thisIsWhereIThink");
							inOut.sendObject(map);
							inOut.sendString("finishedEndScanning", "finished scanning");
							scanner.scanDetect();
							inOut.readString("waiting for next ");
							currentCell.visited = true;
						}
					}
			}
			inOut.cleanUp();
		} catch (Exception l) {
			System.out.print("failed");
			l.printStackTrace();
		}
	}

	private void particleFilterDirection() throws Exception {
		int distance;
		for (Cell[] aMap1 : map) {
			for (int j = 0; j < map.length; j++) {
				if (aMap1[j].contents != 0 && aMap1[j].contents != 1) {
					aMap1[j].contents = 0;
				}
				if (aMap1[j].contents == 0) {
					aMap1[j].chanceOfTemp[ID - 2][0] = 1;
				}
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
		if (Config.Simulated) {
			for (int i = 0; i < map.length; i++) {
				for (int j = 0; j < map.length; j++) {
					if (map[i][j].contents == 0) {
						map[i][j].chanceOfSelfPercent[ID - 2][0] += 0.001;
					}
				}
			}
		}
		recalulatePercent(map);
	}

	private int particleFilter(int xReduction, int yReduction) throws Exception {
		inOut.sendString("readMap", "reading map");
		Cell[][] tempMap = (Cell[][]) inOut.readObj("reading map");
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map.length; j++) {
				map[i][j].chanceOfOccupancy = tempMap[i][j].chanceOfOccupancy;
			}
		}
		int distance = 0;
		distance = scanner.scanForwardDistance(false);
		int[] amountFound = new int[11];
		amountFound[distance]++;
		for (int i = 0; i < 4; i++) {
			amountFound[distance = scanner.scanForwardDistance(false)]++;
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
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				if (map[i][j].contents == 0) {
					boolean valid = true;
					for (int k = 0; k < distance; k++) {
						int xValue = i + (k * xReduction) + xReduction;
						int yValue = j + (k * yReduction) + yReduction;
						if (map[xValue][yValue].contents == 1) {
							valid = false;
							break;
						}
					}
					if (valid) {
						int xValue = i + (distance * xReduction) + xReduction;
						int yValue = j + (distance * yReduction) + yReduction;
						if (!(xValue >= map.length || xValue < 0 || yValue >= map[0].length || yValue < 0)) {
							int howMany = 0;
							double chance = 0;
							for (int s = 0; s < map[xValue][yValue].chanceOfOccupancy.length; s++) {
								if (map[xValue][yValue].chanceOfOccupancy[s] > 0.20) {
									if (chance < map[xValue][yValue].chanceOfOccupancy[s]) {
										chance = map[xValue][yValue].chanceOfOccupancy[s];
									}
									howMany++;
								}
							}
							if (map[xValue][yValue].contents != 1 && howMany == 1 && chance > 0.50) {
								map[i][j].chanceOfTemp[ID - 2][0] *= chance;
							}
						}
					} else if (!valid) {
						if (Config.Simulated)
							map[i][j].chanceOfTemp[ID - 2][0] = 0.1;
						else
							map[i][j].chanceOfTemp[ID - 2][0] *= 0.1;
					}
				}
			}
		}
		return distance;
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
		for (Cell[] aMap23 : map2) {
			for (int j = 0; j < map2.length; j++) {

				sum += aMap23[j].chanceOfTemp[ID - 2][0];

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
		for (Cell[] aMap22 : map2) {
			for (int j = 0; j < map2.length; j++) {
				aMap22[j].chanceOfTemp[ID - 2][0] = aMap22[j].chanceOfTemp[ID - 2][0] / sum;
			}
		}
		double sum2 = 0;
		for (Cell[] aMap21 : map2) {
			for (int j = 0; j < map2.length; j++) {
				aMap21[j].chanceOfSelfPercent[ID - 2][0] = (aMap21[j].chanceOfSelfPercent[ID - 2][0]
						* aMap21[j].chanceOfTemp[ID - 2][0]) + 0.00001;
				sum2 += aMap21[j].chanceOfSelfPercent[ID - 2][0];
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
		for (Cell[] aMap2 : map2) {
			for (int j = 0; j < map2.length; j++) {
				aMap2[j].chanceOfSelfPercent[ID - 2][0] = aMap2[j].chanceOfSelfPercent[ID - 2][0] / sum2;
			}
		}
	}

}
