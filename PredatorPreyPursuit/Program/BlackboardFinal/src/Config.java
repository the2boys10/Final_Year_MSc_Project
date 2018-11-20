class Config
{
    /**
     * Would we like to run the server in a simulated manner (Won't ssh into robots)
     */
    public static final boolean Simulated = true;
    /**
     * What scanning policy would you like to use for the prey
     * <ul>
     *     <li>1: Prey will use its ultrasonic sensor if possible.</li>
     *     <li>2: Prey will scan from server.</li>
     * </ul>
     */
    public static final int PreyScanning = 2;
    /**
     * What scanning policy would you like to use for the predator in relation to start of turn scanning
     * <ul>
     *     <li>1: Pred can only scan in-front of itself at start of round, after turning and after round.</li>
     *     <li>2: Pred can scan in all axis's every turn.</li>
     * </ul>
     */
    public static final int PredScanPolicy = 1;
    /**
     * What scanning policy would you like to use for the prey
     * <ul>
     *     <li>1: Prey will use its ultrasonic sensor if possible.</li>
     *     <li>2: Prey will scan from server.</li>
     * </ul>
     */
    public static final int PredScanning = 2;
    /**
     * What location would you like the prey to be in on simulation startup
     * <ul>
     *     <li>{-1,-1}: Prey will not show on the board, as such simulated scanning will not work</li>
     *     <li>{!=-1,!=-1}: Prey will show on board as well as the movements it makes.</li>
     * </ul>
     */
    public static final int[] preyLocation = new int[]{4, 3};
    /**
     * The Ip of the prey
     */
    public static final String preyIP = "172.20.1.131";
    /**
     * The Ip of predator 1
     */
    public static final String pred1IP = "172.20.1.129";
    /**
     * The Ip of predator 2
     */
    public static final String pred2IP = "172.20.1.130";
    /**
     * The Ip of predator 3
     */
    public static final String pred3IP = "172.20.1.132";
    /**
     * The Ip of predator 4
     */
    public static final String pred4IP = "172.20.1.128";
    /**
     * The system will be ran in rapid testing mode, use this if you have configured the paths in ComPrey and Com
     */
    public static final boolean rapidTesting = false;
    /**
     * Would you like to use a dissipationGrid
     */
    public static boolean dissipationGrid = true;
    /**
     * What policy would you like to use for the prey
     * <ul>
     *     <li>1: Prey can Rotate once during turn.</li>
     *     <li>2: Prey can Rotate multiple during turn.</li>
     *     <li>3: Prey can Rotate and Move during turn.</li>
     *     <li>4: Prey will be controlled by the user and be able to turn and move.</li>
     * </ul>
     */
    public static int PreyPolicy = 1;
    /**
     * What policy would you like to use for the predator
     * <ul>
     *     <li>1: Pred can Rotate or Move during turn.</li>
     *     <li>2: Pred can Rotate and Move during turn.</li>
     * </ul>
     */
    public static int PredPolicy = 1;
    /**
     * What policy would you like to use for the prey
     * <ul>
     *     <li>1: Pred searches using bfs search no incentive to move round others</li>
     *     <li>2: Pred searches using bfs search and counts rotations as moves no incentive to move round others.</li>
     *     <li>3: Pred searches using bfs search which penalises passing though preds</li>
     *     <li>4: Pred searches using bfs search which penalises passing though preds and counts rotations as moves.</li>
     * </ul>
     */
    public static int PredSearch = 3;
    public static boolean instantReward = false;
}
