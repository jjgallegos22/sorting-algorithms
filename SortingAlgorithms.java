import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/////////////////////////////////////////////////////////////
public class SortingAlgorithms
{
//-----------------------------------------------------------
    public static void main( String[] args )
    {
        //int[] array = { 6, 3, 1, 9, 7, 8, 4, 2, 5 };
        //printArray( array );
        //selectionSort( array, new AtomicInteger() );
        //insertionSort( array, new AtomicInteger() );
        //bubbleSort( array, new AtomicInteger() );
        //shellSort( array, new AtomicInteger() );
        //shuffleSort( array );
        //mergeSort( array, new AtomicInteger() );
        //mergeSortBU( array, new AtomicInteger() );
        //quickSort( array, new AtomicInteger() );
        //quickSort3( array, new AtomicInteger() );
        //heapSort( array, new AtomicInteger() );
        //printArray( array );
        sortingAnalysis();
    }
//-----------------------------------------------------------
    public static void sortingAnalysis()
    {
        final int MAX_SIZE = 15; // 50,000 items
        final int NUM_ALGO = 8;
        final boolean print = true;
        int[] array = new int[MAX_SIZE];
        int[] copy  = new int[MAX_SIZE];
        long[] time  = new long[NUM_ALGO];
        long before  = 0;
        Random rnd = new Random();
        for ( int i = 0; i < MAX_SIZE; i++ )
        {
            array[i] = rnd.nextInt( 10 * MAX_SIZE );
            copy[i] = array[i];
        }

        // Print Unsorted Array
        if ( print )
        {
            System.out.print("Unsorted Array: ");
            printArray( array );
        }

        //-----------Selection Sort-----------------
        before = System.currentTimeMillis();
        selectionSort( copy, new AtomicInteger() );
        time[0] = System.currentTimeMillis() - before;
        if ( print )
        {
            System.out.print("Selection Sort: ");
            printArray( copy );
        }
        copyArray( array, copy );
        //------------------------------------------


        //-------------Bubble Sort------------------
        before = System.currentTimeMillis();
        bubbleSort( copy, new AtomicInteger() );
        time[1] = System.currentTimeMillis() - before;
        if ( print )
        {
            System.out.print("Bubble Sort   : ");
            printArray( copy );
        }
        copyArray( array, copy );
        //------------------------------------------


        //------------Insertion Sort----------------
        before = System.currentTimeMillis();
        insertionSort( copy, new AtomicInteger() );
        time[2] = System.currentTimeMillis() - before;
        if ( print )
        {
            System.out.print("Insertion Sort: ");
            printArray( copy );
        }
        copyArray( array, copy );
        //------------------------------------------


        //---------------Shell Sort-----------------
        before = System.currentTimeMillis();
        shellSort( copy, new AtomicInteger() );
        time[3] = System.currentTimeMillis() - before;
        if ( print )
        {
            System.out.print("Shell Sort    : ");
            printArray( copy );
        }
        copyArray( array, copy );
        //------------------------------------------


        //---------------Merge Sort-----------------
        before = System.currentTimeMillis();
        mergeSort( copy, new AtomicInteger() );
        time[4] = System.currentTimeMillis() - before;
        if ( print )
        {
            System.out.print("Merge Sort    : ");
            printArray( copy );
        }
        copyArray( array, copy );
        //------------------------------------------


        //----------------Quick Sort----------------
        before = System.currentTimeMillis();
        quickSort( copy, new AtomicInteger() );
        time[5] = System.currentTimeMillis() - before;
        if ( print )
        {
            System.out.print("Quick Sort    : ");
            printArray( copy );
        }
        copyArray( array, copy );
        //------------------------------------------


        //--------Quick Sort 3 Partition------------
        before = System.currentTimeMillis();
        quickSort3( copy, new AtomicInteger() );
        time[6] = System.currentTimeMillis() - before;
        if ( print )
        {
            System.out.print("Quick Sort 3  : ");
            printArray( copy );
        }
        //------------------------------------------

        //-----------Heap Sort----------------------
        before = System.currentTimeMillis();
        heapSort( copy, new AtomicInteger() );
        time[7] = System.currentTimeMillis() - before;
        if ( print )
        {
            System.out.print("Heap Sort     : ");
            printArray( copy );
            System.out.println("");
        }
        //------------------------------------------


        //---------Print Running Times--------------
            System.out.println("Sorting Algorithms Running Times (in ms)");
            System.out.println("Selection Sort: " + time[0] );
            System.out.println("Bubble Sort   : " + time[1] );
            System.out.println("Insertion Sort: " + time[2] );
            System.out.println("Shell Sort    : " + time[3] );
            System.out.println("Merge Sort    : " + time[4] );
            System.out.println("Quick Sort    : " + time[5] );
            System.out.println("Quick Sort 3  : " + time[6] );
            System.out.println("Heap Sort     : " + time[7] );
        //------------------------------------------
    }
//-----------------------------------------------------------
    public static void selectionSort( int[] array, AtomicInteger ops )
    {
        for ( int i = 0; i < array.length-1; i++ )
        {
            int min = i;
            for ( int j = i + 1; j < array.length; j++ )
            {
                ops.incrementAndGet();
                if ( array[min] > array[j] ) { min = j; }
            }
            swap( array, i, min );
        }
    }
//-----------------------------------------------------------
    public static void insertionSort( int[] array, AtomicInteger ops )
    {
        for ( int i = 1; i < array.length; i++ )
        {
            int j = i;
            while ( j > 0 && ( ops.incrementAndGet() >= 0 ) && array[j-1] > array[j] )
            {
                swap( array, j-1, j );
                j--;
            }
        }
    }
//-----------------------------------------------------------
    public static void bubbleSort( int[] array, AtomicInteger ops )
    {
        boolean isSorted = false;

        while ( !isSorted )
        {
            isSorted = true;
            for ( int i = 0; i < array.length - 1; i++ )
            {
                ops.incrementAndGet();
                if ( array[i] > array[i+1] )
                {
                    swap( array, i, i+1 );
                    isSorted = false;
                }
            }
        }
    }
//-----------------------------------------------------------
    public static void shellSort( int[] array, AtomicInteger ops )
    {
        // Use increment sequence: 3x + 1...compute h
        int h = 1;
        while ( h < array.length )
        {
            int temp = ( 3 * h ) + 1;
            if ( temp > array.length ) { break; }
            h = temp;
        }

        while ( h > 0 )
        {
            for ( int i = 0; (i+h) < array.length; i++ )
            {
                int j = i + h;
                while ( ( (j-h) >= 0 ) && ( ops.incrementAndGet() >= 0 ) && array[j-h] > array[j] )
                {
                    swap( array, j-h, j );
                    j -= h;
                }
            }
            h = ( h - 1 ) / 3; // Go down sequences: h = 3x+1 => x = (h-1)/3
        }
    }
//-----------------------------------------------------------
    public static void shuffleSort( int[] array ) // will randomize an array
    {
        int[] shuffle = new int[array.length];
        Random random = new Random();
        for ( int i = 0; i < array.length; i ++ )
        {
            shuffle[i] = random.nextInt( 999 + 1 );
        }

        // Now Sort shuffle array while making the same changes
        // for array this will shuffle
        for ( int i = 0; i < shuffle.length-1; i++ )
        {
            int min = i;
            for ( int j = i + 1; j < shuffle.length; j++ )
            {
                if ( shuffle[min] > shuffle[j] ) { min = j; }
            }
            swap( shuffle, i, min );
            swap( array, i, min );
        }
        System.out.print("Shuffled Array Sorted: " );
        printArray( shuffle );
    }
//-----------------------------------------------------------
    public static void mergeSort( int[] array, AtomicInteger ops )
    {
        int[] aux = new int[array.length];
        sort( array, aux, 0, array.length-1, ops );
    }
//-----------------------------------------------------------
    public static void sort( int[] arr, int[] aux, int lo, int hi, AtomicInteger ops )
    {
        if ( lo >= hi ) return;
        int mid = lo + ( hi - lo )/2;
        sort( arr, aux, lo, mid, ops );
        sort( arr, aux, mid+1, hi, ops );
        merge( arr, aux, lo, mid, hi, ops );
    }
//-----------------------------------------------------------
    public static void merge( int[] arr, int[] aux, int lo, int mid, int hi, AtomicInteger ops )
    {
        // Copy current contents of array to auxiliary array
        for ( int k = lo; k <= hi; k++ ) { aux[k] = arr[k]; }

        int i = lo;
        int j = mid+1;
        for ( int k = lo; k <= hi; k++ )
        {
            ops.incrementAndGet();
            if      ( i > mid )         arr[k] = aux[j++];
            else if ( j > hi  )         arr[k] = aux[i++];
            else if ( aux[i] < aux[j] ) arr[k] = aux[i++];
            else                        arr[k] = aux[j++];

        }
    }
//-----------------------------------------------------------
    public static void mergeSortBU( int[] array, AtomicInteger ops )
    {
        // Implements Bottom-Up merge sort version
        int[] aux = new int[array.length];
        for ( int n = 1; n < array.length; n += n )
        {
            for ( int i = 0; i < array.length-n; i += n+n )
            {
                int lo = i;
                int mid = i+n-1;
                int hi = Math.min( i+n+n-1, array.length-1 );
                merge( array, aux, lo, mid, hi, ops );
            }
        }
    }
//-----------------------------------------------------------
    public static void quickSort( int[] array, AtomicInteger ops )
    {
        // Can add shuffle here to guarantee NlogN performance
        sort( array, 0, array.length-1, ops );
    }
//-----------------------------------------------------------
    public static void sort( int[] array, int lo, int hi, AtomicInteger ops )
    {
        if ( hi <= lo ) return;
        int j = partition( array, lo, hi, ops );
        sort( array, lo, j-1, ops );
        sort( array, j+1, hi, ops );
    }
//-----------------------------------------------------------
    public static int partition( int[] array, int lo, int hi, AtomicInteger ops )
    {
        int i = lo;
        int j = hi+1;
        int value = array[lo];
        while ( true )
        {
            while( ops.incrementAndGet() >= 0 && array[++i] < value ) if ( i == hi ) break;
            while( ops.incrementAndGet() >= 0 && array[--j] > value ) if ( j == lo ) break;

            if ( i >= j ) break;
            swap( array, i, j );
        }
        swap( array, lo, j );
        return j;
    }
//-----------------------------------------------------------
    public static void quickSort3( int[] array, AtomicInteger ops )
    {
        sort3( array, 0, array.length-1, ops );
    }
//-----------------------------------------------------------
    public static void sort3( int[] array, int lo, int hi, AtomicInteger ops )
    {
        if ( lo >= hi ) return;
        int lt = lo, gt = hi, i = lo;
        int value = array[lo];
        while( i <= gt )
        {
            ops.incrementAndGet();
            if      ( array[i] < value ) swap( array, lt++, i++ );
            else if ( array[i] > value ) swap( array, i, gt-- );
            else                         i++;
        }
        // a[lo..lt-1] < v = a[lt..gt] < a[gt+1..hi].
        sort3( array, lo, lt-1, ops );
        sort3( array, gt+1, hi, ops );
    }
//-----------------------------------------------------------
    public static void heapSort( int[] array, AtomicInteger ops )
    {
        // Heaps work with starting items beginning at 1
        // So create a copy and expand by 1.
        int[] copy = new int[array.length+1];
        System.arraycopy( array, 0, copy, 1, array.length );
        int N = copy.length - 1;
        heapify( copy, N, ops );
        while ( N > 1 )
        {
            swap( copy, 1, N-- );
            sink( copy, 1, N, ops );
        }
        System.arraycopy( copy, 1, array, 0, array.length );
    }
//-----------------------------------------------------------
    public static void heapify( int[] array, int N, AtomicInteger ops )
    {
        for ( int k = N/2; k >= 1; k-- )
            sink( array, k, N, ops );
    }
//-----------------------------------------------------------
    public static void sink( int[] array, int k, int N, AtomicInteger ops )
    {
        int j;
        while ( 2*k <= N )
        {
            j = 2*k;
            ops.incrementAndGet();
            if ( j < N && array[j] < array[j+1] ) j++;
            ops.incrementAndGet();
            if ( array[k] > array[j] ) break;
            swap( array, k, j );
            k = j;
        }
    }
//-----------------------------------------------------------
    public static void swap( int[] array, int i, int j )
    {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
//-----------------------------------------------------------
    public static void printArray( int[] array )
    {
        for ( int i = 0; i < array.length; i++ )
        {
            System.out.print( array[i] + " " );
        }
        System.out.println("");
    }
//-----------------------------------------------------------
    public static void copyArray( int[] array, int[] copy )
    {
        System.arraycopy( array, 0, copy, 0, array.length );
    }
//-----------------------------------------------------------
}
/////////////////////////////////////////////////////////////
