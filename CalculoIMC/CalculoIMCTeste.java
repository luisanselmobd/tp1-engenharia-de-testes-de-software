
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import static net.jqwik.api.Arbitraries.doubles;
import static net.jqwik.api.Arbitraries.oneOf;
import net.jqwik.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
public class CalculoIMCTeste {



    private void gerarInput(String valor) {
        InputStream in = new ByteArrayInputStream(valor.getBytes());
        System.setIn(in);
    }

    String mensagemErroPeso = "Peso inválido. Digite um valor numérico entre 0,2 e 700,00.";
    String mensagemErroAltura = "Altura inválida. Digite um valor numérico entre 0,2 e 2,60.";


    @Provide
    Arbitrary<Double> pesoExtremo() {
        return oneOf(
                doubles().lessThan(0.2),
                doubles().greaterThan(700)
        );
    }

    @Provide
    Arbitrary<Double> pesoCorreto() {
        return doubles().between(0.2, 700);
    }

    @Provide
    Arbitrary<Double> alturaExtrema() {
        return oneOf(
                doubles().lessThan(0.2),
                doubles().greaterThan(2.6)
        );
    }

    @Provide
    Arbitrary<Double> alturaCorreta() {
        return doubles().between(0.2, 2.6);
    }

    @Test
    public void calcularPesoComMock() {
        MockedStatic<CalculoIMC> mockedStatic = Mockito.mockStatic(CalculoIMC.class);
        double peso = pesoCorreto().sample();
        double altura = alturaCorreta().sample();
        Double resultado = peso / Math.pow(altura, 2);

        mockedStatic.when(() -> CalculoIMC.calcularPeso(peso, altura)).thenReturn(resultado);

        assertThat(resultado).isEqualTo(CalculoIMC.calcularPeso(peso, altura));
        mockedStatic.verify(() -> CalculoIMC.calcularPeso(peso, altura));
    }

    @Test
    public void calculosValidos() {
        assertEquals(22.86, CalculoIMC.calcularPeso(70, 1.75), 0.01);
        assertEquals(24.69, CalculoIMC.calcularPeso(80, 1.80), 0.01);
        assertEquals(18.36, CalculoIMC.calcularPeso(50, 1.65), 0.01);
    }

    @Property
    void calculosValidos(@ForAll @From("pesoCorreto") Double peso, @ForAll @From("alturaCorreta") Double  altura) {
        double imc = CalculoIMC.calcularPeso(peso, altura);
        assertThat(imc).isGreaterThanOrEqualTo(0);
    }

    @Test
    public void classificacaoIMCCorreta() {
        assertEquals("Magreza grave", CalculoIMC.classificarIMC(15.9));
        assertEquals("Magreza moderada", CalculoIMC.classificarIMC(16));
        assertEquals("Magreza leve", CalculoIMC.classificarIMC(17));
        assertEquals("Saudável", CalculoIMC.classificarIMC(18.5));
        assertEquals("Sobrepeso", CalculoIMC.classificarIMC(25));
        assertEquals("Obesidade Grau I", CalculoIMC.classificarIMC(30));
        assertEquals("Obesidade Grau II", CalculoIMC.classificarIMC(35));
        assertEquals("Obesidade Grau III", CalculoIMC.classificarIMC(40));
    }

    @Example
    void imcCasoMinimo() {
        Double resultado = 0.2 / (0.2 * 0.2);
        Double max = 700 / (2.6 * 2.6);
        assertThat(CalculoIMC.calcularPeso(0.2, 0.2)).isEqualTo(resultado);
    }

    @Example
    void imcCasoMaximo() {
        Double resultado = 700 / (2.6 * 2.6);
        assertThat(CalculoIMC.calcularPeso(700, 2.6)).isEqualTo(resultado);
    }

    @Test
    public void valoresComVirgula() {
        assertDoesNotThrow(() -> {
            gerarInput("95,00\n1,75\n");
            CalculoIMC.programaIMC("1");
        });
    }

    @Property
    void valoresComVirgula(@ForAll @From("pesoCorreto") Double peso, @ForAll @From("alturaCorreta") Double  altura) {
        String mensagem = String.format("%f\n%f\n", peso, altura);
        gerarInput(mensagem);
        assertDoesNotThrow(() -> CalculoIMC.programaIMC("1"));
    }

    @Test
    public void pesoMenorQueOPermitido() {
        Exception exception = assertThrows(Exception.class, () -> {
            gerarInput("0\n1.75\n");
            CalculoIMC.programaIMC("1");
        });
        assertEquals(mensagemErroPeso, exception.getMessage());
    }

    @Test
    public void pesoMaiorQueOPermitido() {
        Exception exception = assertThrows(Exception.class, () -> {
            gerarInput("700,01\n1.75\n");
            CalculoIMC.programaIMC("1");
        });
        assertEquals(mensagemErroPeso, exception.getMessage());
    }

    @Property
    public void pesosExtremos(@ForAll @From("pesoExtremo") Double peso, @ForAll @From("alturaCorreta") Double altura) {
        Exception exception = assertThrows(Exception.class, () -> {
            String mensagem = String.format("%f\n%f\n", peso, altura);
            gerarInput(mensagem);
            CalculoIMC.programaIMC("1");
        });
        assertThat(exception.getMessage()).isEqualTo(mensagemErroPeso);
    }

    @Test
    public void pesoFormatoNaoNumerico() {
        Exception exception = assertThrows(Exception.class, () -> {
            gerarInput("100kg\n1.75\n");
            CalculoIMC.programaIMC("1");
        });
        assertEquals(mensagemErroPeso, exception.getMessage());
    }

    @Test
    public void alturaMenorQueAPermitida() {
        Exception exception = assertThrows(Exception.class, () -> {
            gerarInput("100\n0,19\n");
            CalculoIMC.programaIMC("1");
        });
        assertEquals(mensagemErroAltura, exception.getMessage());
    }

    @Test
    public void alturaMaiorQueAPermitida() {
        Exception exception = assertThrows(Exception.class, () -> {
            gerarInput("100\n2,61\n");
            CalculoIMC.programaIMC("1");
        });
        assertEquals(mensagemErroAltura, exception.getMessage());
    }

    @Property
    public void alturasExtremas(@ForAll @From("pesoCorreto") Double peso, @ForAll @From("alturaExtrema") Double altura) {
        Exception exception = assertThrows(Exception.class, () -> {
            String mensagem = String.format("%f\n%f\n", peso, altura);
            gerarInput(mensagem);
            CalculoIMC.programaIMC("1");
        });
        assertThat(exception.getMessage()).isEqualTo(mensagemErroAltura);
    }

    @Test
    public void alturaFormatoNaoNumerico() {
        Exception exception = assertThrows(Exception.class, () -> {
            gerarInput("100\n1,85m\n");
            CalculoIMC.programaIMC("1");
        });
        assertEquals(mensagemErroAltura, exception.getMessage());
    }

}
