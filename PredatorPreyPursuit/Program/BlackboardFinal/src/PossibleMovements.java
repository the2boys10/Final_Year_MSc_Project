/**
 * Container to store information relating to each move by each agent
 */
class PossibleMovements
{
    /**
     * What cell would the agent like to turn to or occupy.
     */
    final Cell moveTo;
    /**
     * Is the agent current facing the cell i.e would it like to occupy it or face it.
     */
    final boolean facingIt;
    /**
     * Whats the identity of the agent wanting to take this move
     */
    final int identity;

    /**
     * Whats the binary representation of the move that the agent would like to take.
     */
    final int move;

    /**
     *
     * @param moveTo What cell would the agent like to face or move into
     * @param facingIt Is the agent currently facing the tile or wanting to turn to it
     * @param identity What is the identity of the agent wanting to take this move
     * @param move What is the move that it would like to take.
     */
    public PossibleMovements(Cell moveTo, boolean facingIt, int identity, int move)
    {
        this.moveTo = moveTo;
        this.facingIt = facingIt;
        this.identity = identity;
        this.move = move;
    }
}
