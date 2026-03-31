import Layout from './Layout';
import CodeEditor from './CodeEditor';
import Problem, { type ProblemProps } from './Problem';

export default function App() {

  const problema: ProblemProps = {
    title: "Extremamente Básico",
    source: {
      autor: "Neilor Tonin",
      instituicao: "URI",
      localizacao: "Brasil",
    },
    content: 'Leia 2 valores inteiros e armazene-os nas variáveis A e B. Efetue a soma de A e B atribuindo o seu resultado na variável X. Imprima X conforme exemplo apresentado abaixo. Não apresente mensagem alguma além daquilo que está sendo especificado e não esqueça de imprimir o fim de linha após o resultado, caso contrário, você receberá "Presentation Error".',
    timeLimit: 1,
    in: "A entrada contém 2 valores inteiros.",
    out: 'Imprima a mensagem "X = " (letra X maiúscula) seguido pelo valor da variável X e pelo final de linha. Cuide para que tenha um espaço antes e depois do sinal de igualdade, conforme o exemplo abaixo.',
    example: [
      {
        in: "10\n9\n",
        out: "X = 19"
      },
      {
        in: "-10\n4\n",
        out: "X = -6"
      },
      {
        in: "15\n-7\n",
        out: "X = 8"
      }
    ]
  }

  return (
    <Layout>
      <Problem problem={problema} />
      <CodeEditor />
    </Layout>
  );
}