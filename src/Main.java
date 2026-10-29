import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

/**
 * Experimentos de ordenação e busca (Projeto da Unidade 1 - EDA/LEDA).
 *
 * Uso:  java Main                      -> tamanhos padrão (50.000, 150.000 e 500.000)
 *       java Main 10000 30000 100000   -> tamanhos personalizados
 *
 * Para cada tamanho são gerados 4 cenários (aleatório, ordenado, inverso e 15% embaralhado).
 * Todos os algoritmos recebem CÓPIAS da mesma entrada. Os resultados vão para o console
 * e para o arquivo resultados.csv (útil para montar tabelas e gráficos do relatório).
 */
public class Main {

    // ---------------- Parâmetros do experimento ----------------
    private static final int[] DEFAULT_SIZES = {50_000, 150_000, 500_000};
    private static final int TOTAL_RUNS = 25;           // execuções por algoritmo
    private static final int WARM_UP_RUNS = 5;          // primeiras execuções descartadas (aquecimento da JVM)
    private static final int MAX_VAL = 100_000;         // valores no intervalo [1, 100.000]
    private static final long SEED = 42;                // semente fixa => experimento reproduzível
    private static final double PARTIAL_FRACTION = 0.15;
    private static final int SEARCH_TARGET = -1;        // elemento ausente (pior caso das buscas)

    // Acima destes tamanhos o algoritmo é ignorado (tempo ou pilha inviáveis)
    private static final int QUADRATIC_LIMIT = 50_000;
    private static final int RECURSIVE_LINEAR_LIMIT = 50_000;

    private static final String CSV_FILE = "resultados.csv";
    private static final String LINE = "-".repeat(88);

    // ---------------- Algoritmos de ordenação avaliados ----------------
    private static final List<Algoritmo> SORTERS = Arrays.asList(
            new Algoritmo("BubbleSort (slide)",          AlgoritmosOrdenacao::bubbleSort, true),
            new Algoritmo("BubbleSort (otimizado)",      AlgoritmosOrdenacao::bubbleSortOptimized, true),
            new Algoritmo("SelectionSort (slide)",       AlgoritmosOrdenacao::selectionSort, true),
            new Algoritmo("SelectionSort (estável)",     AlgoritmosOrdenacao::selectionSortStable, true),
            new Algoritmo("InsertionSort",               AlgoritmosOrdenacao::insertionSort, true),
            new Algoritmo("MergeSort (clássico)",        AlgoritmosOrdenacao::mergeSort, false),
            new Algoritmo("QuickSort (slide)",           AlgoritmosOrdenacao::quickSort, false),
            new Algoritmo("QuickSort (slide + shuffle)", AlgoritmosOrdenacao::quickSortShuffle, false),
            new Algoritmo("CountingSort",                arr -> AlgoritmosOrdenacao.countingSort(arr, MAX_VAL), false),
            new Algoritmo("Arrays.sort() [Java]",        arr -> Arrays.sort(arr), false)
    );

    public static void main(String[] args) throws IOException {
        int[] sizes = parseSizes(args);
        Random rand = new Random(SEED);

        try (PrintWriter csv = new PrintWriter(CSV_FILE, "UTF-8")) {
            csv.println("tipo,tamanho,cenario,algoritmo,unidade,media,desvio_padrao,status");

            System.out.println("=".repeat(88));
            System.out.println("  PROJETO LEDA - EXPERIMENTOS DE ORDENAÇÃO E BUSCA (JAVA)");
            System.out.println("  " + TOTAL_RUNS + " execuções por teste, " + WARM_UP_RUNS + " descartadas (warm-up)");
            System.out.println("=".repeat(88));

            for (int size : sizes) {
                System.out.println("\n" + "=".repeat(88));
                System.out.println("  TAMANHO DO VETOR (N): " + size);
                System.out.println("=".repeat(88));

                // Entrada original de cada cenário (gerada uma única vez por tamanho)
                int[] random = generateRandomArray(size, rand);
                int[] sorted = random.clone();
                Arrays.sort(sorted);
                int[] reverse = generateReverseArray(sorted);
                int[] partial = generatePartialSortedArray(sorted, PARTIAL_FRACTION, rand);

                evaluateSorting("Aleatório", random, csv);
                evaluateSorting("Já ordenado", sorted, csv);
                evaluateSorting("Ordem inversa", reverse, csv);
                evaluateSorting("Parcialmente ordenado (15%)", partial, csv);

                evaluateSearching(sorted, csv);
            }
        }
        System.out.println("\nResultados salvos em " + CSV_FILE);
    }

    // =====================================================================
    // Experimentos
    // =====================================================================

    private static void evaluateSorting(String scenario, int[] base, PrintWriter csv) {
        System.out.println("\n" + LINE);
        System.out.println(" CENÁRIO: " + scenario);
        System.out.println(LINE);
        System.out.printf("%-30s | %16s | %16s | %s%n", "Algoritmo", "Média (ms)", "Desvio (ms)", "Status");
        System.out.println(LINE);

        for (Algoritmo alg : SORTERS) {
            Resultado r = (alg.quadratico && base.length > QUADRATIC_LIMIT)
                    ? Resultado.ignorado("Ignorado (inviável)")
                    : benchmarkSort(alg.operacao, base);
            print(alg.nome, r, 4, 30);
            save(csv, "ordenacao", base.length, scenario, alg.nome, "ms", r, 4);
        }
    }

    private static void evaluateSearching(int[] sorted, PrintWriter csv) {
        String scenario = "Vetor ordenado, alvo ausente (" + SEARCH_TARGET + ")";
        System.out.println("\n" + LINE);
        System.out.println(" BUSCA: " + scenario);
        System.out.println(LINE);
        System.out.printf("%-35s | %12s | %12s | %s%n", "Algoritmo", "Média (ns)", "Desvio (ns)", "Resultado");
        System.out.println(LINE);

        searchRow(csv, sorted, "Busca Linear Iterativa",
                () -> AlgoritmosBusca.linearSearchIterative(sorted, SEARCH_TARGET), true);
        searchRow(csv, sorted, "Busca Linear Recursiva",
                () -> AlgoritmosBusca.linearSearchRecursive(sorted, SEARCH_TARGET),
                sorted.length <= RECURSIVE_LINEAR_LIMIT);
        searchRow(csv, sorted, "Busca Linear Duas Pontas",
                () -> AlgoritmosBusca.linearSearchTwoWay(sorted, SEARCH_TARGET), true);
        searchRow(csv, sorted, "Busca Binária Iterativa",
                () -> AlgoritmosBusca.binarySearchIterative(sorted, SEARCH_TARGET), true);
        searchRow(csv, sorted, "Busca Binária Recursiva",
                () -> AlgoritmosBusca.binarySearchRecursive(sorted, SEARCH_TARGET), true);
    }

    private static void searchRow(PrintWriter csv, int[] sorted, String name, IntSupplier search, boolean enabled) {
        Resultado r = enabled ? benchmarkSearch(search) : Resultado.ignorado("Ignorado (inviável)");
        print(name, r, 0, 35);
        save(csv, "busca", sorted.length, "ordenado", name, "ns", r, 0);
    }

    // =====================================================================
    // Medição
    // =====================================================================

    /** Executa a ordenação TOTAL_RUNS vezes, cada uma sobre uma cópia nova da entrada. */
    private static Resultado benchmarkSort(Consumer<int[]> sorter, int[] base) {
        double[] times = new double[TOTAL_RUNS - WARM_UP_RUNS];
        boolean allSorted = true;

        for (int run = 0; run < TOTAL_RUNS; run++) {
            int[] work = base.clone();            // cópia fora da medição

            long start = System.nanoTime();
            try {
                sorter.accept(work);
            } catch (StackOverflowError e) {
                return Resultado.ignorado("FALHA (StackOverflow)");
            }
            long elapsed = System.nanoTime() - start;

            if (run >= WARM_UP_RUNS) times[run - WARM_UP_RUNS] = elapsed / 1_000_000.0; // ns -> ms
            if (!isSorted(work)) allSorted = false; // verificação fora da medição
        }
        return Resultado.de(times, allSorted ? "OK (ordenado)" : "ERRO");
    }

    /** Executa a busca TOTAL_RUNS vezes. Como o alvo é ausente, o resultado esperado é sempre -1. */
    private static Resultado benchmarkSearch(IntSupplier search) {
        double[] times = new double[TOTAL_RUNS - WARM_UP_RUNS];
        boolean correct = true;

        for (int run = 0; run < TOTAL_RUNS; run++) {
            long start = System.nanoTime();
            int result;
            try {
                result = search.getAsInt();
            } catch (StackOverflowError e) {
                return Resultado.ignorado("FALHA (StackOverflow)");
            }
            long elapsed = System.nanoTime() - start;

            if (run >= WARM_UP_RUNS) times[run - WARM_UP_RUNS] = elapsed; // já em ns
            if (result != -1) correct = false;
        }
        return Resultado.de(times, correct ? "OK (não encontrado)" : "ERRO");
    }

    // =====================================================================
    // Geração dos cenários
    // =====================================================================

    private static int[] generateRandomArray(int size, Random rand) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) arr[i] = rand.nextInt(MAX_VAL) + 1; // [1, MAX_VAL]
        return arr;
    }

    private static int[] generateReverseArray(int[] sorted) {
        int n = sorted.length;
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = sorted[n - 1 - i];
        return arr;
    }

    /**
     * Parte de um vetor ordenado, sorteia ~15% das POSIÇÕES (sem repetir) e
     * embaralha apenas os valores dessas posições, como pede o enunciado.
     */
    private static int[] generatePartialSortedArray(int[] sorted, double fraction, Random rand) {
        int n = sorted.length;
        int k = (int) Math.round(n * fraction);
        int[] arr = sorted.clone();

        // 1) Sorteia k posições distintas (Fisher-Yates parcial sobre os índices)
        int[] pos = new int[n];
        for (int i = 0; i < n; i++) pos[i] = i;
        for (int i = 0; i < k; i++) swap(pos, i, i + rand.nextInt(n - i));

        // 2) Embaralha os valores que estão nessas k posições
        for (int i = k - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            swap(arr, pos[i], pos[j]);
        }
        return arr;
    }

    // =====================================================================
    // Utilitários
    // =====================================================================

    private static boolean isSorted(int[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[i] > arr[i + 1]) return false;
        }
        return true;
    }

    private static void swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    private static int[] parseSizes(String[] args) {
        if (args.length == 0) return DEFAULT_SIZES;
        int[] sizes = new int[args.length];
        for (int i = 0; i < args.length; i++) {
            try {
                sizes[i] = Integer.parseInt(args[i].replace(".", "").replace("_", ""));
                if (sizes[i] < 1) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                System.err.println("Tamanho inválido: '" + args[i] + "'. Use inteiros positivos, ex.: java Main 10000 50000");
                System.exit(1);
            }
        }
        return sizes;
    }

    private static void print(String name, Resultado r, int decimals, int nameWidth) {
        System.out.printf("%-" + nameWidth + "s | %16s | %16s | %s%n",
                name, format(r.media, decimals), format(r.desvio, decimals), r.status);
    }

    private static void save(PrintWriter csv, String type, int size, String scenario,
                             String name, String unit, Resultado r, int decimals) {
        csv.printf(Locale.US, "%s,%d,\"%s\",\"%s\",%s,%s,%s,\"%s\"%n", type, size, scenario, name, unit,
                Double.isNaN(r.media) ? "" : String.format(Locale.US, "%." + decimals + "f", r.media),
                Double.isNaN(r.desvio) ? "" : String.format(Locale.US, "%." + decimals + "f", r.desvio),
                r.status);
        csv.flush();
    }

    private static String format(double value, int decimals) {
        return Double.isNaN(value) ? "N/A" : String.format("%." + decimals + "f", value);
    }

    // =====================================================================
    // Tipos auxiliares
    // =====================================================================

    /** Um algoritmo de ordenação com nome e indicação de custo quadrático. */
    private static final class Algoritmo {
        final String nome;
        final Consumer<int[]> operacao;
        final boolean quadratico;

        Algoritmo(String nome, Consumer<int[]> operacao, boolean quadratico) {
            this.nome = nome;
            this.operacao = operacao;
            this.quadratico = quadratico;
        }
    }

    /** Média e desvio padrão (amostral) dos tempos medidos, mais um texto de status. */
    private static final class Resultado {
        final double media;
        final double desvio;
        final String status;

        private Resultado(double media, double desvio, String status) {
            this.media = media;
            this.desvio = desvio;
            this.status = status;
        }

        static Resultado de(double[] times, String status) {
            double sum = 0;
            for (double t : times) sum += t;
            double mean = sum / times.length;

            double sq = 0;
            for (double t : times) sq += (t - mean) * (t - mean);
            double std = times.length > 1 ? Math.sqrt(sq / (times.length - 1)) : 0;
            return new Resultado(mean, std, status);
        }

        /** Para testes não executados ou que falharam: sem números. */
        static Resultado ignorado(String status) {
            return new Resultado(Double.NaN, Double.NaN, status);
        }
    }
}
