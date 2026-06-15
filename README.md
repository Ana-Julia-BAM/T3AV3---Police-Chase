# CSES Problem Set — Police Chase

## Trabalho Prático 3 — Unidade 3
**Disciplina:** Resolução de Problemas com Grafos  
**Orientador:** Prof. Ricardo Carubbi  

### Problema
* **Nome:** Police Chase  
* **Plataforma:** CSES  
* **Link:** https://cses.fi/problemset/task/1695  
* **Categoria:** Advanced Graphs (Redes de Fluxo / Corte Mínimo)  

### Integrantes do Grupo B
* **Nathan Linhares Dias Malheiros** — 2220227  
* **Ana Julia Benevides Arrais Monteiro** — 2314986  
* **Marcos Vinícius dos Santos e Silva** — 2220232  

### Video Apresentação
* **Link:** https://youtu.be/XOUOh-eOa2g

### Linguagem Utilizada
* Java 21 (64-bit)

---

## Como Executar

### Pré-requisitos
* JDK 21 instalado.
* Estrutura de pastas conforme descrita abaixo.

### Estrutura esperada

```text
T3/
├── README.md
├── src/
│   └── Main.java
├── evidencias/
│   └── AcceptedScreen.png
├── apresentacao/
│   └── apresentacao.pdf
└── dados/
    └── entradas_do_problema.txt
```

### Compilação
Na raiz do projeto, execute:
```bash
javac src/Main.java -d out/
```

### Execução
O programa conta com uma inicialização dinâmica (híbrida): se o arquivo `dados/entradas_do_problema.txt` existir localmente, ele lerá os dados dele (facilitando os testes no VS Code). Caso contrário (como no ambiente de testes do CSES), ele fará o *fallback* automático para a entrada padrão do terminal (`System.in`).

```bash
java -cp out/ Main
```

### Formato da entrada (`entradas_do_problema.txt`)
```text
4 5
1 2
1 3
2 3
3 4
1 4
```

### Saída esperada
```text
2
1 4
3 4
```
*(Nota: Qualquer conjunto válido de $k$ arestas que desconecte a rede é aceito pela plataforma).*

---

## Descrição do Problema
O assaltante Kaaleppi acabou de roubar um banco (cruzamento 1) e está a fugir em direção ao porto (cruzamento $n$). A polícia deseja impedir a fuga interditando o **menor número possível de ruas** da cidade, de modo que não reste nenhuma rota transitável entre o banco e o porto.

O problema exige calcular:
* O número mínimo $k$ de ruas que devem ser fechadas.
* Quais são os pares de cruzamentos que delimitam cada uma dessas $k$ ruas.

### Restrições
* $2 \le n \le 500$ (número de cruzamentos/vértices)
* $1 \le m \le 1000$ (número de ruas/arestas)
* Todas as ruas são bidirecionais e existe no máximo uma rua direta entre dois cruzamentos.

---

## Modelagem como Rede de Fluxo

Para resolver o problema do menor número de interdições, aplicamos o **Teorema do Fluxo Máximo e Corte Mínimo (Max-Flow Min-Cut)**. Uma interdição de rua equivale a cortar uma aresta no grafo. Queremos o corte com o menor custo (capacidade acumulada) possível.

### Vértices
Cada cruzamento de 1 a $n$ representa um vértice na rede de fluxo. 
* **Origem ($s$):** Vértice 1 (Banco), onde a rota de fuga se inicia.
* **Sorvedouro ($t$):** Vértice $n$ (Porto), o destino final que deve ser isolado.

### Arestas e Capacidades
Como as ruas originais são bidirecionais e não possuem pesos (todas têm o mesmo custo de fechamento), modelamos cada rua entre $u$ e $v$ como **duas arestas direcionadas independentes** no grafo residual:
* $u \to v$ com capacidade $c(u, v) = 1$
* $v \to u$ com capacidade $c(v, u) = 1$

A atribuição de **capacidade unitária (1)** garante que o valor do Fluxo Máximo da rede corresponderá exatamente à quantidade mínima de arestas que precisam ser removidas para cindir o grafo.

### Representação Adotada
Foi utilizada uma lista de adjacência baseada em objetos estruturados (`List<Edge>[] adj`), onde cada objeto encapsula os ponteiros residuais necessários:
* `from` / `to`: Vértices de extremidade.
* `capacity`: Capacidade máxima da aresta direcionada (sempre 1 na inicialização).
* `flow`: Fluxo atualmente alocado na aresta.
* `rev`: Referência em memória para a sua aresta reversa oposta, permitindo atualização instantânea do grafo residual em tempo $O(1)$.

---

## Algoritmo Utilizado

### Edmonds-Karp com Extração de Corte

O método de Ford-Fulkerson foi implementado através do algoritmo de **Edmonds-Karp**, que utiliza uma Busca em Largura (**BFS**) para encontrar os caminhos aumentantes mais curtos em número de arestas, garantindo a convergência algorítmica estável.

#### 1. Inicialização
Todos os fluxos de arestas (`edge.flow`) são zerados. As capacidades residuais iniciais de ida e volta são definidas como 1.

#### 2. Busca por Caminhos Aumentantes (BFS)
A cada iteração, uma BFS parte da origem (1) buscando alcançar o sorvedouro ($n$), trafegando apenas por arestas cuja capacidade residual seja estritamente positiva:
$$\text{capacidade residual} = \text{capacity} - \text{flow} > 0$$
O algoritmo armazena o caminho percorrido mapeando os predecessores no vetor `parentEdge[]`.

#### 3. Atualização do Grafo Residual
Ao atingir o sorvedouro, como a rede é unitária, o gargalo do caminho é deterministicamente $1$. O algoritmo retrocede o caminho atualizando as capacidades:
* Incrementa o fluxo da aresta direta: `e.flow += 1`
* Decrementa o fluxo da aresta reversa correspondente: `e.rev.flow -= 1`

O processo se repete até que a BFS não consiga mais alcançar o nó $n$.

#### 4. Reconstrução do Corte Mínimo (Resposta Final)
Após o esgotamento do fluxo, o corte mínimo é determinado da seguinte forma:
1. Uma BFS final é executada a partir da origem (1) para identificar o conjunto $S$ de todos os vértices que ainda são **alcançáveis** por arestas com capacidade residual $> 0$. Os vértices não alcançáveis formam o conjunto $T$.
2. Varrem-se as arestas originais da entrada do problema. Se uma aresta conecta um vértice pertencente a $S$ a um vértice pertencente a $T$ (ou vice-versa), significa que ela está completamente saturada e faz parte do gargalo do corte. Essa rua é adicionada à lista de interdições.

---

## Análise de Complexidade

### Tempo

| Etapa | Complexidade Geral | No Cenário Unitário |
| :--- | :--- | :--- |
| Leitura da Entrada e Grafo | $O(m)$ | $O(m)$ |
| Execução do Edmonds-Karp | $O(V \cdot E^2)$ | $O(E \cdot |f|) \to O(E \cdot V)$ |
| BFS Final e Extração do Corte | $O(V + E)$ | $O(V + E)$ |
| **Total** | $O(V \cdot E^2)$ | **$O(E \cdot V)$** |

Em redes com capacidades unitárias, o fluxo máximo $|f|$ é limitado pelo número de vértices $V$. Portanto, a complexidade prática do Edmonds-Karp cai para **$O(E \cdot V)$**.  
Com $V \le 500$ e $E \le 1000$, o número máximo de operações estimadas é de $500 \times 1000 = 5 \times 10^5$, executando em aproximadamente **0.03 segundos**, o que é amplamente seguro para o limite de 1.00s da plataforma.

### Memória
A estrutura consome armazenamento para a lista de adjacências contendo as $2m$ arestas direcionadas e os vetores auxiliares de tamanho $n+1$ (`visited`, `parentEdge`). A complexidade de espaço total é **$O(V + E)$**, consumindo menos de 5 MB de memória, ficando muito abaixo do limite de 512 MB estipulado.

---

## Casos Especiais

| Situação | Comportamento do Algoritmo |
| :--- | :--- |
| **Grafo desconectado na entrada** | A primeira BFS falha em alcançar $n$. O fluxo máximo calculado é 0. O corte mínimo impresso será 0 e nenhuma aresta é listada. |
| **Múltiplos caminhos de comprimento igual** | A BFS escolhe o primeiro na ordem de indexação da lista de adjacências. Como o Teorema Max-Flow Min-Cut é invariante, qualquer escolha leva a um valor de corte correto. |
| **Arestas que não afetam o escoamento** | Arestas redundantes dentro do próprio conjunto $S$ ou conjunto $T$ nunca são saturadas a ponto de isolar os conjuntos, sendo ignoradas na filtragem final do corte. |

---

## Evidência de Submissão Aceita

A imagem comprovando o resultado **Accepted** no juiz online do CSES está disponível no repositório em:  
`evidencias/accepted.jpeg`

---

## Referências
1. **Enunciado do problema (CSES 1695):** https://cses.fi/problemset/task/1695
2. **CP-Algorithms — Edmonds-Karp & Max-Flow:** https://cp-algorithms.com/graph/edmonds_karp.html
3. **Livro Base da Disciplina:** SEDGEWICK, Robert; WAYNE, Kevin. *Algorithms, 4th Edition*. Capítulo 6 (Contexto de redes de fluxo).
