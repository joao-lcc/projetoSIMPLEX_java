import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.*;
class AvaliadorExpressoes {
	static String regex = "^(\\d+\\*x\\d+\\s*([-+])\\s*)*\\d+\\*x\\d+$";

	static public boolean validarExpressao(String expressao) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(expressao);

		return matcher.matches();
	}
}

class Janela extends JFrame{
	JPanel p1 = new JPanel();
	JPanel p2 = new JPanel();
	// texto inicial
	// TODO: Se quiser deixar bonitinho tem que fazer mais um panel com GridLayout (acho)
	JLabel mensagem = new JLabel("Bem-vindo a calculdora de forma padrao! Formato: a1*x1 + a2*x2 + ... + an*xn");
	int quant_restricoes = 0;
	int MaiorQuantidadeVariaveis = 0;
	// função objetiva
	JLabel label_fo = new JLabel("Insira aqui a funcao objetiva:");
	JTextField fo = new JTextField(15);
	JComboBox problema = new JComboBox();

	// restrições
	JLabel label_rest = new JLabel("Insira aqui a restricao: ");
	JTextField rest = new JTextField(20);
	JComboBox sinal = new JComboBox();
	JTextField b = new JTextField(4);
	JButton add = new JButton("Adicionar");
	
	// calcular
	JButton calcular = new JButton("Calcular");
	
	Janela(){
		super("Metodo Simplex");
		
		// estruturas de dados que serão utilizadas
		ArrayList<ArrayList<HashMap<String, Integer>>> matriz_A = new ArrayList<>();
		ArrayList vetor_x = new ArrayList();
		ArrayList vetor_b = new ArrayList();
		//Contador de restrições
		AvaliadorExpressoes avaliador = new AvaliadorExpressoes();
		
		p1.setLayout(new BorderLayout());
		p2.setLayout(new FlowLayout());
		setLayout(new FlowLayout());

		// texto inicial
		add(mensagem);

		// função objetiva
		p1.add(label_fo);
		problema.addItem("max");
		problema.addItem("min");
		p1.add(problema, BorderLayout.WEST);
		p1.add(fo, BorderLayout.CENTER);
		add(p1);

		// restrições
		p2.add(label_rest);
		p2.add(rest);
		sinal.addItem("<=");
		sinal.addItem("=");
		sinal.addItem(">=");
		p2.add(sinal);
		p2.add(b);
		p2.add(add);
		add(p2);

		// calcular
		add(calcular);

		// tratamento de eventos 
		add.addActionListener(new ActionListener() {
			
            @Override
            public void actionPerformed(ActionEvent e) {
                matriz_A.add(new ArrayList());
                String restricao = rest.getText();
				String resultado = b.getText();
				
                // valida restricao -> precisa arrumar 
				if (avaliador.validarExpressao(restricao)) {
					System.out.println("Expressao = " + restricao + " | Termo independente = " + resultado);
					vetor_b.add(resultado);
					rest.setText("");
					
				} else {
					JOptionPane.showMessageDialog(null, "A restricao nao esta no formato correto",
					"Expressao invalida", JOptionPane.ERROR_MESSAGE);
					setVisible(false);
					new Janela();
				}

				//valida b
				
                try {
                    // Tenta converter o texto em um número inteiro
					int numero = Integer.parseInt(resultado);
					// Divide a expressao da restricao no tipo an*xn
                    String partes[] = restricao.split("[+]");
                    // Define-se a quantidade de variaveis contidas na restrição
                    int quant_variaveis = partes.length;    
                    if(quant_restricoes == 0){
						MaiorQuantidadeVariaveis = quant_variaveis;
                    }else{
						if(quant_variaveis > MaiorQuantidadeVariaveis)
							//Utilizado para armazenar o maior numero de variaveis usadas em uma restricao inseridas (Matriz A nxn)
							MaiorQuantidadeVariaveis = quant_variaveis;
					}

                    for(String elemento: partes){
						//Separa-se os coeficientes das variaveis correspondentes
						String unitaria[] = elemento.split("[*]");
						//Cria-se uma relação entre coeficiente e variavel (HashMap)
						HashMap<String,Integer> rest = new HashMap<>();
						rest.put(unitaria[1], Integer.parseInt(unitaria[0]));
						if(!vetor_x.contains(unitaria[1]))
							vetor_x.add(unitaria[1]);
						matriz_A.get(quant_restricoes).add(rest);
					}
					b.setText("");
                } catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(null, "O elemento nao vetor b nao está na forma correta",
				"Expressao invalida", JOptionPane.ERROR_MESSAGE);
                }
              quant_restricoes+=1;
			}
        });
		calcular.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
				
				// verifica função objetiva -> precisa arrumar
				String funcao = fo.getText();
                if (!avaliador.validarExpressao(funcao)) {
					JOptionPane.showMessageDialog(null, "Expressao invalida",
				"A funcao objetiva nao esta no formato correto", JOptionPane.ERROR_MESSAGE);
				}
			
				Collections.sort(vetor_x);
				System.out.println("---------------------------------------------------------------");
				System.out.println("VETOR X - VARIAVEIS");
				System.out.println("Total de variaveis usadas: " + MaiorQuantidadeVariaveis);
				for(int i = 0; i < vetor_x.size(); i++){
					System.out.println(vetor_x.get(i));
				}
				System.out.println("---------------------------------------------------------------");
				//Todas as restricoes são verificadas para que contenham todas as variaveis Xn (se a expressao nao possuir Xn cria-se com coef. correspodente valendo 0)
				for(int i = 0; i < matriz_A.size(); i++){
					if(vetor_x.size() != matriz_A.get(i).size()){
						for(int j = 0; j < vetor_x.size(); j++){
							int verifica = 0;
							for(HashMap<String, Integer> mapa: matriz_A.get(i)){
								if(verifica != 1){
									if (mapa.containsKey(vetor_x.get(j))) {
										verifica = 1;
									}
								}
							}
							if(verifica == 0){
								HashMap<String,Integer> rest = new HashMap<>();
								String var_x = vetor_x.get(j)+ "";
								rest.put(var_x, 0);
								matriz_A.get(i).add(rest);
							}
						
						}
					}
				}
				for(int i = 0; i < matriz_A.size(); i++){
					ordena(matriz_A.get(i));
				}
				
				System.out.println("MATRIZ A - RESTRICOES");
				System.out.println("Tamanho: " + matriz_A.size());
				for(int i = 0; i < matriz_A.size(); i++){
					System.out.println(matriz_A.get(i));
				}
				System.out.println("---------------------------------------------------------------");
				System.out.println("VETOR B - TERMOS INDEPENDENTES");
				for(int i = 0; i < vetor_b.size(); i++){
					System.out.println(vetor_b.get(i));
				}
				System.out.println("---------------------------------------------------------------");
			}
        });
		setSize(600, 200);
		setVisible(true);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
	
	public void ordena(ArrayList<HashMap<String, Integer>> vetMap) {
        ArrayList<String>chavesOrdenadas = new ArrayList<>();
        for (HashMap<String, Integer> mapa : vetMap) {
            chavesOrdenadas.addAll(mapa.keySet());
        }
        chavesOrdenadas = new ArrayList<>(new HashSet<>(chavesOrdenadas));
        Collections.sort(chavesOrdenadas);
        HashMap<String, Integer> mapaTemporario = new HashMap<>();
        for (String chave : chavesOrdenadas) {
            for (HashMap<String, Integer> mapa : vetMap) {
                if (mapa.containsKey(chave)) {
                    mapaTemporario.put(chave, mapa.get(chave));
                    mapa.remove(chave);
                }
            }
        }
  
        vetMap.clear();
        for (String chave : chavesOrdenadas) {
            HashMap<String, Integer> novoMapa = new HashMap<>();
            novoMapa.put(chave, mapaTemporario.get(chave));
            vetMap.add(novoMapa);
        }
    }

    
	public static void main(String[] args){
		System.out.println("inicializando programa...");
		new Janela();
	}
}
