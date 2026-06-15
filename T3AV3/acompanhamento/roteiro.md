# Ficha de Acompanhamento — Grupo B
**Problema:** CSES 1695 — Police Chase  
**Algoritmo:** Edmonds-Karp  
**Link:** https://cses.fi/problemset/task/1695

---

## 1. Resumo do problema

Um fugitivo saiu de um banco e está tentando chegar ao porto da cidade. As ruas ligam cruzamentos e são de mão dupla. A polícia quer bloquear o menor número possível de ruas para garantir que não exista nenhum caminho entre o banco e o porto.

O problema pede o **corte mínimo** do grafo: o conjunto de menor tamanho de arestas que, ao ser removido, desconecta completamente a origem do destino. Pelo Teorema Max-Flow Min-Cut, esse número é igual ao fluxo máximo da rede.

---

## 2. Interpretação da entrada e da saída

**Entrada:**

```
4 5       → n=4 cruzamentos, m=5 ruas
1 2       → rua entre cruzamentos 1 e 2
1 3
2 3
3 4
1 4
```

- O banco fica no cruzamento `1`, o porto no cruzamento `n` (aqui, `4`).
- Cada linha seguinte descreve uma rua bidirecional entre dois cruzamentos `a` e `b`.

**Saída:**

```
2         → fechar 2 ruas é suficiente
3 4       → fechar a rua entre 3 e 4
1 4       → fechar a rua entre 1 e 4
```

- Primeiro imprime `k`, o número mínimo de ruas a fechar.
- Depois imprime os `k` pares correspondentes às ruas.

---

## 3. Modelagem da rede de fluxo

**Vértices:** cada cruzamento da cidade vira um vértice na rede. Com `n=4`, temos os vértices `{1, 2, 3, 4}`.

**Fonte e sumidouro:**
- Fonte `S = 1` — é onde o fugitivo parte (banco).
- Sumidouro `T = n = 4` — é onde ele quer chegar (porto).

Essa escolha faz sentido direto pelo enunciado: queremos cortar toda comunicação entre esses dois pontos.

**Arestas:** cada rua `(a, b)` é bidirecional, então vira **duas arestas direcionadas** na rede:

```
a → b  com capacidade 1
b → a  com capacidade 1
```

**Por que capacidade 1?**  
Fechar uma rua é uma decisão binária — ou fecha ou não fecha. Não existe "fechar meia rua". Cada rua tem custo unitário de remoção, então capacidade 1 modela exatamente isso. O fluxo máximo conta quantas ruas no mínimo precisam ser saturadas para bloquear toda passagem.

**Grafo resultante para a instância do enunciado:**

```
          2
         / \
        /   \
       1-----3
        \   /
         \ /
          4
```

Com as arestas direcionadas (cada linha bidirecional vira duas):

```
1→2 (1)   2→1 (1)
1→3 (1)   3→1 (1)
2→3 (1)   3→2 (1)
3→4 (1)   4→3 (1)
1→4 (1)   4→1 (1)
```

---

## 4. Justificativa: por que Edmonds-Karp

Ford-Fulkerson com busca genérica pode escolher caminhos ruins repetidamente. Com capacidades inteiras, no pior caso ele faz O(max_flow) iterações — se as capacidades fossem grandes, isso seria lento.

Edmonds-Karp usa **BFS** para sempre encontrar o caminho aumentante mais curto (em número de arestas). Isso garante complexidade **O(V · E²)**, independente dos valores de capacidade.

Para este problema:
- `n ≤ 500`, `m ≤ 1000` — grafo pequeno.
- Todas as capacidades são **1**, então o fluxo máximo é no máximo `m = 1000`.
- Com capacidades unitárias, o número de iterações na prática se aproxima de O(E√V), bem dentro do limite de 1 segundo.
- BFS é simples de implementar corretamente e evita os casos patológicos do DFS.

---

## 5. Instância pequena

Usamos o exemplo do próprio enunciado:

```
Entrada:
4 5
1 2
1 3
2 3
3 4
1 4

Saída esperada:
2
3 4
1 4
```

Caminhos possíveis de 1 até 4 no grafo original:
- `1 → 4`
- `1 → 3 → 4`
- `1 → 2 → 3 → 4`

---

## 6. Execução manual passo a passo

### Estado inicial do grafo residual

Cada par de arestas com capacidade 1 em cada direção:

```
1→2: 1    2→1: 1
1→3: 1    3→1: 1
1→4: 1    4→1: 1
2→3: 1    3→2: 1
3→4: 1    4→3: 1
```

---

### Iteração 1

**BFS de 1 até 4:**

```
Fila: [1]
Expande 1 → vizinhos com cap > 0: {2, 3, 4}
4 encontrado! Caminho: 1 → 4
```

**Gargalo:** `min(cap(1→4)) = 1`

**Atualiza residual:**
```
1→4: 1 − 1 = 0   (saturada)
4→1: 1 + 1 = 2   (reversa aumenta)
```

**Fluxo acumulado: 1**

---

### Iteração 2

**BFS de 1 até 4:**

```
Fila: [1]
Expande 1 → vizinhos com cap > 0: {2, 3}   (1→4 saturada)
Expande 2 → vizinhos: {1 já visitado, 3}
Expande 3 → vizinhos: {1 já visitado, 2 já visitado, 4}
4 encontrado! Caminho: 1 → 3 → 4
```

**Gargalo:** `min(cap(1→3), cap(3→4)) = min(1, 1) = 1`

**Atualiza residual:**
```
1→3: 1 − 1 = 0   (saturada)
3→1: 1 + 1 = 2   (reversa aumenta)
3→4: 1 − 1 = 0   (saturada)
4→3: 1 + 1 = 2   (reversa aumenta)
```

**Fluxo acumulado: 2**

---

### Iteração 3

**BFS de 1 até 4:**

```
Fila: [1]
Expande 1 → vizinhos com cap > 0: {2}   (1→3 e 1→4 saturadas)
Expande 2 → vizinhos: {1 já visitado, 3}
Expande 3 → vizinhos: {2 já visitado, 1 já visitado}
               3→4: cap = 0 → bloqueada
Fila vazia. Vértice 4 não foi alcançado.
```

**BFS falhou. Algoritmo termina.**

**Fluxo máximo = 2**

---

### Grafo residual ao final

```
1→2: 1    2→1: 1
1→3: 0 ✗  3→1: 2
1→4: 0 ✗  4→1: 2
2→3: 1    3→2: 1
3→4: 0 ✗  4→3: 2
```

---

## 7. Verificação da resposta final

Para recuperar as arestas do corte, faz-se uma **BFS/DFS no grafo residual final** a partir do vértice 1, marcando tudo que ainda é alcançável com capacidade residual > 0.

**BFS de recuperação:**

```
Começa em 1.
1→2: cap=1 → adiciona 2
1→3: cap=0 → bloqueada
1→4: cap=0 → bloqueada
De 2:
  2→3: cap=1 → adiciona 3
De 3:
  3→4: cap=0 → bloqueada
  3→1: cap=2 → 1 já visitado
  3→2: cap=1 → 2 já visitado

Conjunto S (alcançáveis) = {1, 2, 3}
Conjunto T̄ (não alcançáveis) = {4}
```

**Identificação das arestas do corte:**

Percorre todas as arestas originais do grafo. A aresta `(a, b)` faz parte do corte se `a ∈ S` e `b ∈ T̄`:

| Aresta | a ∈ S? | b ∈ T̄? | Corte? |
|--------|--------|--------|--------|
| (1, 2) | sim    | não    | não    |
| (1, 3) | sim    | não    | não    |
| (2, 3) | sim    | não    | não    |
| (3, 4) | sim    | sim    | **sim** |
| (1, 4) | sim    | sim    | **sim** |

**Resposta:**

```
2
3 4
1 4
```

Confere com a saída esperada. ✓

> **Observação:** a aresta `1→3` foi saturada durante o algoritmo, mas o vértice `3` ainda é alcançável via `1→2→3`. Por isso `(1,3)` não entra no corte — ambos os seus extremos estão em S. O corte é determinado pela fronteira entre S e T̄, não pelas arestas saturadas.

