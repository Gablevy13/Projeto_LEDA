# Análise de Algoritmos de Busca e Ordenação (Java)

Projeto da **Unidade 1 de Estrutura de Dados (EDA/LEDA 2026.2)**. O programa mede o tempo de
vários algoritmos de **ordenação** e **busca** sobre vetores de inteiros (`int[]`) e compara os
resultados com a complexidade teórica de cada um.

## Algoritmos avaliados

| Tipo      | Algoritmos                                                                                                                                                                          |
|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Ordenação | BubbleSort (slide e otimizado), SelectionSort (slide e estável), InsertionSort, MergeSort, QuickSort (slide e slide + shuffle), CountingSort e `Arrays.sort()` (referência do Java) |
| Busca     | Linear iterativa, linear recursiva, linear duas pontas, binária iterativa e binária recursiva                                                                                       |

## Requisitos

- **JDK 11 ou superior** (precisa do compilador `javac`, não só do JRE).
  Confira com: `java -version` e `javac -version`.

## Estrutura

```
projeto/
├── Main.java                 # roda os experimentos e imprime/salva os resultados
├── AlgoritmosOrdenacao.java  # implementações dos algoritmos de ordenação
├── AlgoritmosBusca.java      # implementações dos algoritmos de busca
└── README.md
```

## Como executar

Abra um terminal **dentro da pasta do projeto** (onde estão os `.java`) e rode:

```bash
javac -encoding UTF-8 *.java
java Main
```

Isso executa a experiência completo com os tamanhos do enunciado: **50.000, 150.000 e 500.000**
elementos.

### Teste rápido

Para conferir se está tudo funcionando sem esperar o experimento completo, informe tamanhos menores:

```bash
java Main 2000 8000 20000
```

Você pode passar quantos tamanhos quiser (separados por espaço).

### Quanto tempo demora?

O experimento completo pode levar **vários minutos** (dezenas, dependendo da máquina). O que mais pesa
são os algoritmos quadráticos (Bubble, Selection e Insertion) em N = 50.000, pois cada um roda
25 vezes em 4 cenários. Para N maiores que 50.000 eles são ignorados de propósito.

## O que o programa faz

1. Gera, para cada tamanho, a entrada de **4 cenários**:
   aleatório, já ordenado, ordem inversa e parcialmente ordenado (~15% das posições embaralhadas).
2. Executa cada algoritmo **25 vezes**, **descartando as 5 primeiras** (aquecimento da JVM). A média e o
   desvio padrão usam as 20 restantes.
3. Cada execução recebe uma **cópia** da mesma entrada original, então um algoritmo nunca vê a saída de outro.
4. A ordenação é **verificada depois de cada execução**, fora da medição de tempo.
5. As buscas procuram o valor `-1` (ausente) em um vetor ordenado, que é o pior caso de todas elas.

Os valores dos vetores ficam no intervalo **[1, 100.000]**, com repetições permitidas. A semente
aleatória é fixa (`42`), então o experimento é reproduzível.

## Saída

- **Console:** uma tabela por cenário, com média e desvio padrão (ordenação em **ms**, busca em **ns**) e status.
- **`resultados.csv`:** os mesmos dados em formato de planilha (criado na pasta onde você rodou o programa),
  pronto para importar no Excel/Google Planilhas e montar os gráficos do relatório.

Colunas do CSV: `tipo, tamanho, cenario, algoritmo, unidade, media, desvio_padrao, status`

uickSort (slide) em vetores ordenados, então deixe claro no relatório.

## Ajustando o experimento

Os parâmetros ficam no topo do `Main.java`:

| Constante                     | Função                                                     |
|-------------------------------|------------------------------------------------------------|
| `TOTAL_RUNS` / `WARM_UP_RUNS` | Execuções totais e descartadas (aquecimento)               |
| `QUADRATIC_LIMIT`             | Maior N em que Bubble/Selection/Insertion ainda rodam      |
| `RECURSIVE_LINEAR_LIMIT`      | Maior N para a busca linear recursiva                      |
| `PARTIAL_FRACTION`            | Fração embaralhada no cenário parcialmente ordenado (0,15) |
| `SEED`                        | Semente aleatória                                          |

## Problemas comuns

- **`javac` não é reconhecido:** você tem só o JRE. Instale um JDK (por exemplo, Temurin ou OpenJDK).
- **`Could not find or load main class Main`:** rode o comando dentro da pasta dos arquivos, depois de compilar.
- **Acentos aparecendo como `?` ou símbolos estranhos no terminal:** rode com
  `java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 Main` (no Windows, antes execute `chcp 65001`).
  Isso afeta só a exibição; o CSV sai em UTF-8.
- **`OutOfMemoryError`:** improvável com esses tamanhos, mas dá para aumentar a memória com `java -Xmx2g Main`.
