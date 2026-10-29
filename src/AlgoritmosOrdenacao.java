import java.util.Arrays;
import java.util.Random;

/**
 * Algoritmos de ordenação para vetores de inteiros (ordem crescente, in-place
 * exceto onde indicado).
 *
 * | Algoritmo              | Melhor     | Médio      | Pior       | Estável | Memória extra |
 * |------------------------|------------|------------|------------|---------|---------------|
 * | BubbleSort (slide)     | O(n²)      | O(n²)      | O(n²)      | sim     | O(1)          |
 * | BubbleSort otimizado   | O(n)       | O(n²)      | O(n²)      | sim     | O(1)          |
 * | SelectionSort (slide)  | O(n²)      | O(n²)      | O(n²)      | não     | O(1)          |
 * | SelectionSort estável  | O(n²)      | O(n²)      | O(n²)      | sim     | O(1)          |
 * | InsertionSort          | O(n)       | O(n²)      | O(n²)      | sim     | O(1)          |
 * | MergeSort              | O(n log n) | O(n log n) | O(n log n) | sim     | O(n)          |
 * | QuickSort (Lomuto)     | O(n log n) | O(n log n) | O(n²)      | não     | O(log n)*     |
 * | CountingSort           | O(n + k)   | O(n + k)   | O(n + k)   | --**    | O(k)          |
 *
 * *  Pilha de recursão; no pior caso do QuickSort vira O(n) e pode estourar.
 * ** Para int primitivo a estabilidade é irrelevante (valores iguais são indistinguíveis).
 *    k = tamanho do intervalo de valores (aqui, 100.000).
 */
public class AlgoritmosOrdenacao {

    private static final long SHUFFLE_SEED = 42; // semente fixa => resultados reproduzíveis

    // ---------------------------------------------------------------
    // Algoritmos quadráticos
    // ---------------------------------------------------------------

    /** BubbleSort da aula: sempre faz todas as passadas. */
    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                if (arr[j] > arr[j + 1]) swap(arr, j, j + 1);
            }
        }
    }

    /** BubbleSort que para assim que uma passada completa não faz nenhuma troca. */
    public static void bubbleSortOptimized(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                if (arr[j] > arr[j + 1]) {
                    swap(arr, j, j + 1);
                    swapped = true;
                }
            }
            if (!swapped) break; // já ordenado
        }
    }

    /** SelectionSort da aula: troca o menor com a posição atual (pode desfazer a ordem de iguais). */
    public static void selectionSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < n; j++) {
                if (arr[j] < arr[minIndex]) minIndex = j;
            }
            swap(arr, i, minIndex);
        }
    }

    /**
     * SelectionSort estável: em vez de trocar, desloca os elementos uma casa
     * para a direita e insere o menor na posição i, preservando a ordem relativa dos iguais.
     */
    public static void selectionSortStable(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < n; j++) {
                if (arr[j] < arr[minIndex]) minIndex = j; // "<" estrito: pega o primeiro menor
            }
            int min = arr[minIndex];
            System.arraycopy(arr, i, arr, i + 1, minIndex - i); // desloca [i, minIndex) uma casa à direita
            arr[i] = min;
        }
    }

    /** InsertionSort: insere cada elemento na posição correta do prefixo já ordenado. */
    public static void insertionSort(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            int key = arr[i];
            int j = i - 1;
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    // ---------------------------------------------------------------
    // MergeSort
    // ---------------------------------------------------------------

    /**
     * MergeSort clássico (dividir e conquistar). Usa um único vetor auxiliar,
     * alocado uma vez, em vez de criar dois vetores novos a cada intercalação.
     */
    public static void mergeSort(int[] arr) {
        if (arr.length < 2) return;
        mergeSortRec(arr, new int[arr.length], 0, arr.length - 1);
    }

    private static void mergeSortRec(int[] arr, int[] aux, int left, int right) {
        if (left >= right) return;
        int mid = left + (right - left) / 2;
        mergeSortRec(arr, aux, left, mid);
        mergeSortRec(arr, aux, mid + 1, right);
        merge(arr, aux, left, mid, right);
    }

    /** Intercala arr[left..mid] e arr[mid+1..right], ambos já ordenados. */
    private static void merge(int[] arr, int[] aux, int left, int mid, int right) {
        System.arraycopy(arr, left, aux, left, right - left + 1);
        int i = left;
        int j = mid + 1;
        for (int k = left; k <= right; k++) {
            if (i > mid)               arr[k] = aux[j++]; // metade esquerda acabou
            else if (j > right)        arr[k] = aux[i++]; // metade direita acabou
            else if (aux[j] < aux[i])  arr[k] = aux[j++];
            else                       arr[k] = aux[i++]; // empate: pega a esquerda (estável)
        }
    }

    // ---------------------------------------------------------------
    // QuickSort
    // ---------------------------------------------------------------

    /**
     * QuickSort da aula (partição de Lomuto, pivô = último elemento).
     * Em vetores já ordenados ou inversos degenera para O(n²) e a recursão
     * fica com profundidade n, podendo causar StackOverflowError.
     */
    public static void quickSort(int[] arr) {
        quickSortRec(arr, 0, arr.length - 1);
    }

    /** QuickSort com embaralhamento prévio: torna o pior caso improvável (esperado O(n log n)). */
    public static void quickSortShuffle(int[] arr) {
        shuffle(arr);
        quickSortRec(arr, 0, arr.length - 1);
    }

    private static void quickSortRec(int[] arr, int low, int high) {
        if (low < high) {
            int p = partition(arr, low, high);
            quickSortRec(arr, low, p - 1);
            quickSortRec(arr, p + 1, high);
        }
    }

    /** Partição de Lomuto: deixa o pivô na posição final e retorna seu índice. */
    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = low - 1; // última posição da região "<= pivô"
        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) {
                i++;
                swap(arr, i, j);
            }
        }
        swap(arr, i + 1, high);
        return i + 1;
    }

    /** Embaralhamento de Fisher-Yates. */
    private static void shuffle(int[] arr) {
        Random rnd = new Random(SHUFFLE_SEED);
        for (int i = arr.length - 1; i > 0; i--) {
            swap(arr, rnd.nextInt(i + 1), i);
        }
    }

    // ---------------------------------------------------------------
    // CountingSort
    // ---------------------------------------------------------------

    /**
     * Ordenação por contagem para valores inteiros no intervalo [0, maxVal].
     * Conta quantas vezes cada valor aparece e reescreve o vetor em ordem.
     * Lança ArrayIndexOutOfBoundsException se algum valor estiver fora do intervalo.
     */
    public static void countingSort(int[] arr, int maxVal) {
        int[] count = new int[maxVal + 1];
        for (int num : arr) count[num]++;

        int index = 0;
        for (int value = 0; value <= maxVal; value++) {
            int c = count[value];
            if (c > 0) {
                Arrays.fill(arr, index, index + c, value);
                index += c;
            }
        }
    }

    // ---------------------------------------------------------------
    // Utilitário
    // ---------------------------------------------------------------

    private static void swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }
}
