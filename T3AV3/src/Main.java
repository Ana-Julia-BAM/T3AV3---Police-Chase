import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.StringTokenizer;

public class Main {
    
    // Classe que representa uma aresta direcionada no grafo residual
    static class Edge {
        int from, to;
        int capacity, flow;
        Edge rev; // Referência para a aresta reversa

        public Edge(int from, int to, int capacity) {
            this.from = from;
            this.to = to;
            this.capacity = capacity;
            this.flow = 0;
        }
    }

    static int n, m;
    static List<Edge>[] adj;
    static List<Edge> originalEdges;

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException {
        // --- LOGICA DE LEITURA HIBRIDA (ARQUIVO LOCAL VS PLATAFORMA) ---
        File fileInput = new File("dados/entradas_do_problema.txt");
        InputStream is;

        if (fileInput.exists()) {
            // Se o arquivo existir (Rodando localmente no seu VS Code), le dele
            is = new FileInputStream(fileInput);
        } else {
            // Se nao existir (Rodando nos servidores do CSES), le da entrada padrao
            is = System.in;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringTokenizer st;
        // ---------------------------------------------------------------

        String firstLine = br.readLine();
        if (firstLine == null) return;
        st = new StringTokenizer(firstLine);

        n = Integer.parseInt(st.nextToken());
        m = Integer.parseInt(st.nextToken());

        adj = new ArrayList[n + 1];
        for (int i = 1; i <= n; i++) {
            adj[i] = new ArrayList<>();
        }
        originalEdges = new ArrayList<>();

        // Leitura das ruas (arestas bidirecionais)
        for (int i = 0; i < m; i++) {
            String line = br.readLine();
            if (line == null) break;
            st = new StringTokenizer(line);
            int u = Integer.parseInt(st.nextToken());
            int v = Integer.parseInt(st.nextToken());

            // Criamos o par de arestas direcionadas com capacidade unitária (1)
            Edge e1 = new Edge(u, v, 1);
            Edge e2 = new Edge(v, u, 1);
            e1.rev = e2;
            e2.rev = e1;

            adj[u].add(e1);
            adj[v].add(e2);
            originalEdges.add(e1); // Mantém o registro para mapear o corte depois
        }

        // Executa o algoritmo de Edmonds-Karp
        edmondsKarp(1, n);

        // Busca em largura (BFS) final no grafo residual para achar os vertices alcancaveis
        boolean[] visited = new boolean[n + 1];
        Queue<Integer> q = new LinkedList<>();
        q.add(1);
        visited[1] = true;

        while (!q.isEmpty()) {
            int curr = q.poll();
            for (Edge e : adj[curr]) {
                // Se ainda houver capacidade residual livre
                if (e.capacity - e.flow > 0 && !visited[e.to]) {
                    visited[e.to] = true;
                    q.add(e.to);
                }
            }
        }

        // Reconstrói o corte mínimo com base nas arestas que dividem os vértices visitados dos não-visitados
        List<Edge> cutEdges = new ArrayList<>();
        for (Edge e : originalEdges) {
            if ((visited[e.from] && !visited[e.to]) || (!visited[e.from] && visited[e.to])) {
                cutEdges.add(e);
            }
        }

        // Impressão otimizada usando StringBuilder
        StringBuilder sb = new StringBuilder();
        sb.append(cutEdges.size()).append("\n");
        for (Edge e : cutEdges) {
            sb.append(e.from).append(" ").append(e.to).append("\n");
        }
        System.out.print(sb);
        
        br.close();
    }

    // Algoritmo de Edmonds-Karp (Max-Flow via BFS)
    private static void edmondsKarp(int source, int sink) {
        Edge[] parentEdge = new Edge[n + 1];

        while (true) {
            Arrays.fill(parentEdge, null);
            Queue<Integer> q = new LinkedList<>();
            q.add(source);

            while (!q.isEmpty()) {
                int curr = q.poll();
                if (curr == sink) break;

                for (Edge e : adj[curr]) {
                    if (parentEdge[e.to] == null && e.to != source && e.capacity - e.flow > 0) {
                        parentEdge[e.to] = e;
                        q.add(e.to);
                    }
                }
            }

            // Se o sorvedouro não foi alcançado, atingimos o fluxo máximo
            if (parentEdge[sink] == null) break;

            // Atualiza as capacidades ao longo do caminho aumentante (gargalo é sempre 1)
            for (int curr = sink; curr != source; curr = parentEdge[curr].from) {
                Edge e = parentEdge[curr];
                e.flow += 1;
                e.rev.flow -= 1;
            }
        }
    }
}