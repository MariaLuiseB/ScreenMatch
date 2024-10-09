package br.com.alura.ScreenMatch.principal;

import br.com.alura.ScreenMatch.model.DadosEpisodio;
import br.com.alura.ScreenMatch.model.DadosSerie;
import br.com.alura.ScreenMatch.model.Episodio;
import br.com.alura.ScreenMatch.service.ConsumoApi;
import br.com.alura.ScreenMatch.service.ConverterDados;
import br.com.alura.ScreenMatch.model.DadosTemporada;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Principal {
    Scanner leitor = new Scanner(System.in);
    private final  String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private ConsumoApi consumo = new ConsumoApi();
    private ConverterDados conversor = new ConverterDados();

    public void exibeMenu() {
        System.out.println("Digite o nome da série para busca.");
        var nomeSerie = leitor.nextLine(); // pega o nome da serie

        //dividir o endereco para receber do usuário
        //ENDERECO: "https://www.omdbapi.com/?t=gilmore+girls&apikey=6585022c"
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);

        //Agora vc pode pedir serie
        DadosSerie dadosSerie = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dadosSerie);

        List<DadosTemporada> temporadas = new ArrayList<>();
		for (int i = 1; i <= dadosSerie.totalTemporadas(); i++) {
			json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
			DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
			temporadas.add(dadosTemporada);
		}
		temporadas.forEach(System.out::println);


        for (int i = 0; i < dadosSerie.totalTemporadas(); i++) {
            List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
            for (int j = 0; j < episodiosTemporada.size(); j++) {
                System.out.println(episodiosTemporada.get(j).titulo()); // imprime só os titulos de cada episodio
            }
        }
        temporadas.forEach(t -> t.episodios()
                    .forEach(e -> System.out.println(e.titulo())));

        //PEGAR OS TOP 5 MELHORES EPISÓDIOS:

        //1º TRANSFORMA A LISTA DENTRO DA LISTA EM UMA LISTA SÓ
        //Problema: Percorrer uma coleção dentro da outra, uma serie tem varias temporadas que tem varios episodios
        List<DadosEpisodio> dadosEpisodios = temporadas.stream()//Cria um stream a partir da lista de temporadas
                .flatMap(t -> t.episodios().stream()) // Para cada temporada (t), transforma a lista de episódios dessa temporada em um único stream contínuo de episódios.
                .collect(Collectors.toList()); // Coleta todos os episódios de cada tmeporada t resultantes do flatMap em uma lista

//        // 2º FILTRA OS 5 EPISÓDIOS QUE TEM AS MELHORES PONTUAÇÕES
//        dadosEpisodios.stream()
//                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A")) // ignore as avaliacoes que tem N/A
//                .sorted(Comparator.comparing(DadosEpisodio::avaliacao))
//                .limit(5)
//                .map(e -> e.titulo().toUpperCase())
//                .forEach(System.out::println);

        // LIDANDO COM OS DADOS DO EPISÓDIO
        List<Episodio> episodios = temporadas.stream()//Cria um stream a partir da lista de temporadas
                .flatMap(t -> t.episodios().stream() // Para cada temporada (t), transforma a lista de episódios dessa temporada em um único stream contínuo de episódios.
                .map(d -> new Episodio(t.numero(), d)))
        .collect((Collectors.toList()));

       episodios.forEach(System.out::println);

       // MANIPULAÇÃO DE DATAS: BUSCANDO A PARTIR DA DATA
//        System.out.println("A partir de que ano você deseja ver os episódios?");
//        var ano = leitor.nextInt();
//        LocalDate dataBusca = LocalDate.of(ano, 1, 1);
//        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

//        episodios.stream()
//                .filter(e -> e.getDataDeLancamento() != null
//                        && e.getDataDeLancamento().isAfter(dataBusca))
//                .forEach(e-> System.out.println(
//                        "\nTemporada:         " + e.getTemporada() +
//                        "\nEpisódio:          " + e.getTitulo() +
//                         "\nData de Lancamento:" + e.getDataDeLancamento().format(formatador)
//                ));

        // ENCONTRANDO A PRIMEIRA OCORRÊNCIA DE BUSCA

//        System.out.println("\nDigite um trecho do título do episódio.");
//        var trechoTitulo = leitor.nextLine();
//        Optional<Episodio> episodioBuscado = episodios.stream()
//                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase())) // coloca tudo em maiúsculo pra comparar
//                .findFirst();
//
//        if(episodioBuscado.isPresent()) {
//            System.out.println("Episódio [" +episodioBuscado + "] encontrado.");
//            System.out.println("Temporada [ " + episodioBuscado.get().getTemporada() +" ]");
//        } else {
//            System.out.println("Episódio não encontrado.");
//        }

        // ESTATISTICAS -> MOSTRANDO A AVALIAÇÃO DE CADA TEMPORADA
        // Teremos uma temporada e a média das avaliações daquela temporada

        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter(e->e.getAvaliacao() >0.0) // Pegue apenas as avaliações que foram avaliadas e não tenha N/A
                .collect(Collectors.groupingBy(Episodio::getTemporada, // pega o número da temporada
                         Collectors.averagingDouble(Episodio::getAvaliacao))); // faz a média aritmética das avaliações de cada episódio da temporada
        System.out.println(avaliacoesPorTemporada);


        //COLETANDO ESTATÍSTICAS
        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
        System.out.println("\nESTATÍSTICAS!");
        System.out.println("Média:  " + est.getAverage());
        System.out.println("Melhor episódio:  " + est.getMax());
        System.out.println("Pior episódio:  " + est.getMin());
        System.out.println("Quantidade de episódios:  " + est.getCount());
    }
}