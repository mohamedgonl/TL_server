package util.algorithms;

import java.util.ArrayList;
import java.util.function.Function;

public class BinaryHeap<T>
{
    public ArrayList<T> content;
    public Function<T, Double> scoreFunction;

    public BinaryHeap(Function<T, Double> scoreFunction)
    {
        this.content = new ArrayList<>();
        this.scoreFunction = scoreFunction;
    }

    public void push(T element)
    {
        // Add the new element to the end of the array.
        this.content.add(element);

        // Allow it to sink down.
        this.sinkDown(this.content.size() - 1);
    }

    public T pop()
    {
        // Store the first element so we can return it later.
        T result = this.content.get(0);
        // Get the element at the end of the array.
        T end = this.content.remove(this.content.size() - 1);
        // If there are any elements left, put the end element at the
        // start, and let it bubble up.
        if (this.content.size() > 0)
        {
            this.content.set(0, end);
            this.bubbleUp(0);
        }
        return result;
    }

    public void remove(T node)
    {
        int i = this.content.indexOf(node);

        // When it is found, the process seen in 'pop' is repeated
        // to fill up the hole.
        T end = this.content.remove(this.content.size() - 1);

        if (i != this.content.size() - 1)
        {
            this.content.set(i, end);

            if (this.scoreFunction.apply(end) < this.scoreFunction.apply(node))
            {
                this.sinkDown(i);
            } else
            {
                this.bubbleUp(i);
            }
        }
    }

    public int size()
    {
        return this.content.size();
    }

    public void rescoreElement(T node)
    {
        this.sinkDown(this.content.indexOf(node));
    }

    public void sinkDown(int n)
    {
        // Fetch the element that has to be sunk.
        T element = this.content.get(n);

        // When at 0, an element can not sink any further.
        while (n > 0)
        {

            // Compute the parent element's index, and fetch it.
            int parentN = ((n + 1) >> 1) - 1;
            T parent = this.content.get(parentN);
            // Swap the elements if the parent is greater.
            if (this.scoreFunction.apply(element) < this.scoreFunction.apply(parent))
            {
                this.content.set(parentN, element);
                this.content.set(n, parent);
                // Update 'n' to continue at the new position.
                n = parentN;
            }
            // Found a parent that is less, no need to sink any further.
            else
            {
                break;
            }
        }
    }

    public void bubbleUp(int n)
    {
        // Look up the target element and its score.
        int length = this.content.size();
        T element = this.content.get(n);
        Double elemScore = this.scoreFunction.apply(element);

        while (true)
        {
            // Compute the indices of the child elements.
            int child2N = (n + 1) << 1;
            int child1N = child2N - 1;
            // This is used to store the new position of the element, if any.
            int swap = -1;//init null
            Double child1Score = (double) 0;
            // If the first child exists (is inside the array)...
            if (child1N < length)
            {
                // Look it up and compute its score.
                T child1 = this.content.get(child1N);
                child1Score = this.scoreFunction.apply(child1);

                // If the score is less than our element's, we need to swap.
                if (child1Score < elemScore)
                {
                    swap = child1N;
                }
            }

            // Do the same checks for the other child.
            if (child2N < length)
            {
                T child2 = this.content.get(child2N);
                double child2Score = this.scoreFunction.apply(child2);
                if (child2Score < (swap == -1 ? elemScore : child1Score))
                {
                    swap = child2N;
                }
            }

            // If the element needs to be moved, swap it, and continue.
            if (swap != -1)
            {
                this.content.set(n, this.content.get(swap));
                this.content.set(swap, element);
                n = swap;
            }
            // Otherwise, we are done.
            else
            {
                break;
            }
        }
    }
}
