/**
 * Algoritmos de busca em vetores de inteiros.
 *
 * Todos retornam o índice do elemento encontrado ou -1 se ele não existir.
 * As buscas binárias exigem vetor previamente ORDENADO.
 *
 * | Algoritmo                     | Melhor caso | Pior caso | Memória extra       |
 * |-------------------------------|-------------|-----------|---------------------|
 * | Linear iterativa              | O(1)        | O(n)      | O(1)                |
 * | Linear recursiva              | O(1)        | O(n)      | O(n) (pilha)        |
 * | Linear duas pontas            | O(1)        | O(n)      | O(1)                |
 * | Binária iterativa             | O(1)        | O(log n)  | O(1)                |
 * | Binária recursiva             | O(1)        | O(log n)  | O(log n) (pilha)    |
 */
public class AlgoritmosBusca {

    // ---------------------------------------------------------------
    // Busca linear
    // ---------------------------------------------------------------

    /** Percorre o vetor da esquerda para a direita. */
    public static int linearSearchIterative(int[] arr, int target) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == target) return i;
        }
        return -1;
    }

    /**
     * Versão recursiva da busca linear.
     * Atenção: o Java não otimiza recursão de cauda, então vetores muito grandes
     * estouram a pilha (StackOverflowError).
     */
    public static int linearSearchRecursive(int[] arr, int target) {
        return linearSearchRecursive(arr, target, 0);
    }

    private static int linearSearchRecursive(int[] arr, int target, int index) {
        if (index >= arr.length) return -1;
        if (arr[index] == target) return index;
        return linearSearchRecursive(arr, target, index + 1);
    }

    /**
     * Busca linear de duas pontas: confere o início e o fim a cada passo,
     * caminhando para o centro. Faz o mesmo número de comparações da busca
     * linear comum; só ajuda quando o alvo está perto de uma das extremidades.
     */
    public static int linearSearchTwoWay(int[] arr, int target) {
        int left = 0;
        int right = arr.length - 1;
        while (left <= right) {
            if (arr[left] == target) return left;
            if (arr[right] == target) return right;
            left++;
            right--;
        }
        return -1;
    }

    // ---------------------------------------------------------------
    // Busca binária (vetor ordenado)
    // ---------------------------------------------------------------

    /** A cada passo descarta metade do intervalo de busca. */
    public static int binarySearchIterative(int[] arr, int target) {
        int left = 0;
        int right = arr.length - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2; // evita overflow de (left + right)
            if (arr[mid] == target) return mid;
            if (arr[mid] < target) left = mid + 1;
            else right = mid - 1;
        }
        return -1;
    }

    /** Versão recursiva da busca binária (profundidade máxima ~ log2 n). */
    public static int binarySearchRecursive(int[] arr, int target) {
        return binarySearchRecursive(arr, target, 0, arr.length - 1);
    }

    private static int binarySearchRecursive(int[] arr, int target, int left, int right) {
        if (left > right) return -1;
        int mid = left + (right - left) / 2;
        if (arr[mid] == target) return mid;
        if (arr[mid] > target) return binarySearchRecursive(arr, target, left, mid - 1);
        return binarySearchRecursive(arr, target, mid + 1, right);
    }
}
