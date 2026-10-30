# Análise de Algoritmos de Busca e Ordenação (Java)

Projeto da **Unidade 1 de Estrutura de Dados (EDA/LEDA 2026.2)**. O programa mede o tempo de
vários algoritmos de **ordenação** e **busca** sobre vetores de inteiros (`int[]`) e compara os
resultados com a complexidade teórica de cada um.

## Algoritmos avaliados

| Tipo | Algoritmos |
|------|-----------|
| Ordenação | BubbleSort (slide e otimizado), SelectionSort (slide e estável), InsertionSort, MergeSort, QuickSort (slide e slide + shuffle), CountingSort e `Arrays.sort()` (referência do Java) |
| Busca | Linear iterativa, linear recursiva, linear duas pontas, binária iterativa e binária recursiva |

## Requisitos

- **JDK 11 ou superior** (precisa do compilador `javac`, não só do JRE).
  Confira com: `java -version` e `javac -version`.

## Estrutura

```
Projeto_LEDA/
├── src/
│   ├── Main.java                 # roda os experimentos e imprime/salva os resultados
│   ├── AlgoritmosOrdenacao.java  # implementações dos algoritmos de ordenação
│   └── AlgoritmosBusca.java      # implementações dos algoritmos de busca
└── README.md
```

## Como executar

Abra um terminal na **raiz do projeto** (a pasta que contém `src/`) e rode:

```bash
javac -encoding UTF-8 -d out src/*.java
java -cp out Main
```

O primeiro comando compila os arquivos para a pasta `out/`; o segundo executa o experimento completo
com os tamanhos do enunciado: **50.000, 150.000 e 500.000** elementos.

> No Windows (CMD ou PowerShell) os mesmos comandos funcionam; se preferir, use `src\*.java`.

### Pelo IntelliJ IDEA

Abra a pasta do projeto, abra `src/Main.java` e clique no botão de executar ao lado do método `main`.
Para passar tamanhos personalizados, use *Run > Edit Configurations > Program arguments*.

### Pelo Eclipse

O repositório não traz os arquivos de projeto do Eclipse (`.project` e `.classpath`), então é preciso criar o
projeto apontando para a pasta clonada. Use o Eclipse IDE for Java Developers com um **JDK 11+** configurado
(*Window > Preferences > Java > Installed JREs*).

1. **Defina a codificação como UTF-8** (evita acentos quebrados): *Window > Preferences > General > Workspace >
   Text file encoding > Other: UTF-8*.
2. **Crie o projeto:** *File > New > Java Project*.
   - Em *Project name*, escreva `Projeto_LEDA`.
   - Desmarque **Use default location** e, em *Location*, escolha a pasta onde você clonou o repositório
     (a que contém `src/`).
   - Mantenha marcado *Create separate folders for sources and class files* / *Detect and configure project
     layout automatically*; o Eclipse reconhece `src/` como pasta de código.
   - Clique em *Finish*.
3. **Execute:** clique com o botão direito em `src/Main.java` e escolha *Run As > Java Application*.
   Os resultados aparecem na aba **Console**.

Alternativa sem apontar para a pasta clonada: crie um projeto novo em *File > New > Java Project* e copie os três
arquivos `.java` de `src/` para a pasta `src` do projeto (pacote padrão, sem `package`).

**Tamanhos personalizados, `-Xss` e `-Xmx`:** *Run > Run Configurations...*, selecione `Main` em *Java Application* e:
- aba **Arguments > Program arguments**: os tamanhos, por exemplo `2000 8000 20000`;
- aba **Arguments > VM arguments**: opções da JVM, por exemplo `-Xss512m` ou `-Xmx2g`;
- aba **Common > Encoding > Other: UTF-8**: acentos corretos no console.

O `resultados.csv` é criado na raiz do projeto. Se ele não aparecer no *Package Explorer*, clique no projeto e
pressione `F5` para atualizar.

### Teste rápido

Para conferir se está tudo funcionando sem esperar o experimento completo, informe tamanhos menores:

```bash
java -cp out Main 2000 8000 20000
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
- **`resultados.csv`:** os mesmos dados em formato de planilha (criado na pasta de onde você rodou o programa),
  pronto para importar no Excel/Google Planilhas e montar os gráficos do relatório.

Colunas do CSV: `tipo, tamanho, cenario, algoritmo, unidade, media, desvio_padrao, status`

### Como ler a coluna Status

| Status | Significado |
|--------|-------------|
| `OK (ordenado)` / `OK (não encontrado)` | Resultado correto |
| `ERRO` | O resultado veio errado (não deveria acontecer) |
| `FALHA (StackOverflow)` | A recursão estourou a pilha. É **esperado** no QuickSort (slide) em vetores já ordenados ou inversos, e é um dos resultados a discutir no relatório |
| `Ignorado (inviável)` | Teste pulado de propósito: muito lento ou recursão profunda demais para esse tamanho |

## Dicas para medições mais confiáveis

- Feche navegador e outros programas pesados; em notebook, deixe na tomada.
- Rode mais de uma vez e compare: pequenas variações entre execuções são normais.
- Não mexa no computador durante o experimento.
- Para deixar a busca linear recursiva rodar em N maiores, aumente a pilha da JVM e ajuste
  `RECURSIVE_LINEAR_LIMIT` no `Main.java`: `java -Xss512m -cp out Main`.
  Note que isso muda o comportamento do QuickSort (slide) em vetores ordenados, então deixe claro no relatório.

## Ajustando o experimento

Os parâmetros ficam no topo do `src/Main.java`:

| Constante | Função |
|-----------|--------|
| `TOTAL_RUNS` / `WARM_UP_RUNS` | Execuções totais e descartadas (aquecimento) |
| `QUADRATIC_LIMIT` | Maior N em que Bubble/Selection/Insertion ainda rodam |
| `RECURSIVE_LINEAR_LIMIT` | Maior N para a busca linear recursiva |
| `PARTIAL_FRACTION` | Fração embaralhada no cenário parcialmente ordenado (0,15) |
| `SEED` | Semente aleatória |

Depois de alterar, compile de novo com o comando de `javac` acima.

## Problemas comuns

- **`javac` não é reconhecido:** você tem só o JRE. Instale um JDK (por exemplo, Temurin ou OpenJDK).
- **`Could not find or load main class Main`:** rode os comandos na raiz do projeto, depois de compilar, e use `-cp out`.
- **Acentos aparecendo como `?` ou símbolos estranhos no terminal:** rode com
  `java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp out Main` (no Windows, antes execute `chcp 65001`).
  Isso afeta só a exibição; o CSV sai em UTF-8.
- **`OutOfMemoryError`:** improvável com esses tamanhos, mas dá para aumentar a memória com `java -Xmx2g -cp out Main`.
