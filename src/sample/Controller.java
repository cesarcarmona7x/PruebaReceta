package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {
    java.util.Map<String,Float> variables=new HashMap<>();
    ArrayList<String>ingredientesasados=new ArrayList<>();
    java.util.Map<String,String>mapaPasosAsoc=new HashMap<>();
    java.util.Map<String,String>pasosGuardados=new HashMap<>();
    String[]ingredientesValidos={"masa seca","metros aproximadamente","chile ancho","chile guajillo","chile pasilla","cebolla","guajolote","carrizo","cabezas de ajo","manteca de puerco","polvo de hornear","carne de puerco","hojas grandes de plátano","pencas de maguey","cuerdas largas"};
    @FXML TextArea textReceta,textIngredientes,textPreparacion,textPreguntas;
    @FXML
    protected void initialize(){
        mapaPasosAsoc.put("titulo","paso0");
        mapaPasosAsoc.put("primer","paso1");
        mapaPasosAsoc.put("segundo","paso2");
        mapaPasosAsoc.put("tercer","paso3");
        mapaPasosAsoc.put("cuarto","paso4");
        mapaPasosAsoc.put("quinto","paso5");
        mapaPasosAsoc.put("porciones","paso6");
        try{
            File f=new File("src/sample/receta.txt");
            BufferedReader br=new BufferedReader(new FileReader(f));
            String line="";
            while((line=br.readLine())!=null){
                textReceta.appendText(line+"\n");
            }
            br.close();
        }catch(Exception ex){
            textReceta.setText(ex.getLocalizedMessage());
        }
    }
    public void procesar(ActionEvent event){
        pasosGuardados.clear();
        ingredientesasados.clear();
        textIngredientes.clear();
        textPreparacion.clear();
        textPreguntas.clear();
        boolean encontroingredientes=false,encontropreparacion=false,encontropreguntas=false;
        String receta=textReceta.getText();
        String[]lineas=receta.split("\n");
        int pason=0;
        for(int i=0;i<lineas.length;i++){
            if(lineas[i].contains("___")){//Encuentra los separadores de sección
                if(encontroingredientes){
                    if(encontropreparacion){
                        if(!encontropreguntas){
                            encontropreguntas=true;
                        }
                    }
                    else{
                        encontropreparacion=true;
                    }
                }
                else{
                    encontroingredientes=true;
                }
            }
            else if(!lineas[i].equals("")){//Descarta líneas vacías
                if(encontroingredientes){
                    if(encontropreparacion){
                        if(encontropreguntas){
                            String respuesta="";
                            if (lineas[i].contains("gramos de chile")) {
                                if(lineas[i].contains("total")){
                                    float total=variables.get("chile ancho")+variables.get("chile guajillo")+variables.get("chile pasilla");
                                    respuesta=String.valueOf(total);
                                }
                                else if(lineas[i].contains("ancho")){
                                    respuesta=String.valueOf(variables.get("chile ancho"));
                                }
                                else if(lineas[i].contains("guajillo")){
                                    respuesta=String.valueOf(variables.get("chile guajillo"));
                                }
                                else if(lineas[i].contains("pasilla")){
                                    respuesta=String.valueOf(variables.get("chile pasilla"));
                                }
                            }
                            if(lineas[i].contains("paso")){
                                String[]componenteslineas=lineas[i].trim().split(" ");
                                String numpaso=mapaPasosAsoc.get(componenteslineas[3]);
                                for(java.util.Map.Entry<String,String> m:pasosGuardados.entrySet()){
                                    if(m.getKey().equals(numpaso)){
                                        respuesta=m.getValue();
                                        break;
                                    }
                                }
                            }
                            if(lineas[i].contains("ingredientes que sean asados")){
                                for(String asado:ingredientesasados){
                                    respuesta+="\n"+asado;
                                }
                            }
                            if(lineas[i].contains("personas")){
                                String[]sublineas=lineas[i].split("\\(");
                                Pattern p= Pattern.compile("[0-9]+");
                                Matcher m1=p.matcher(sublineas[0].trim());
                                Matcher m2=p.matcher(sublineas[1].trim());
                                String npersonas_original=m2.replaceFirst(String.valueOf(variables.get("personas_original")));
                                String replace=sublineas[0].trim();
                                if(sublineas[0].contains("n personas")){
                                    replace=sublineas[0].replace("n personas",String.valueOf(variables.get("personas_original"))+" personas");
                                    int pos1=replace.indexOf(variables.get("personas_original")+" personas");
                                    int pos2=pos1+new String(String.valueOf(variables.get("personas_original"))+" personas").length();
                                    String sub=replace.substring(pos1,pos2);
                                    String[] cantidades=sub.split(" ");
                                    respuesta=reemplazarCantidades(textIngredientes.getText(),variables.get("personas_original"),Float.parseFloat(cantidades[0].trim()));
                                }
                                else{
                                    String[]partesPregunta=sublineas[0].trim().split(" ");
                                    int index=0;
                                    for(int j=0;j<partesPregunta.length;j++){
                                        if(partesPregunta[j].equals("personas")){
                                            index=j;
                                            break;
                                        }
                                    }
                                    respuesta=reemplazarCantidades(textIngredientes.getText(),variables.get("personas_original"),Float.parseFloat(partesPregunta[index-1].trim()));
                                }
                                lineas[i]=lineas[i].replace(sublineas[0],replace);
                                lineas[i]=lineas[i].replace(sublineas[1],npersonas_original);
                            }
                            textPreguntas.appendText(lineas[i].trim()+" "+respuesta+"\n");
                        }
                        else{
                            pasosGuardados.put("paso"+String.valueOf(pason),lineas[i].trim());
                            if(lineas[i].contains("PERSONAS")){
                                String[]componenteslinea=lineas[i].trim().split(" ");
                                variables.put("personas_original",Float.parseFloat(componenteslinea[0]));
                            }
                            textPreparacion.appendText(lineas[i].trim()+"\n");
                            pason++;
                        }
                    }
                    else{
                        String[] componenteslinea=lineas[i].trim().split(" ");
                        for(int j=0;j<ingredientesValidos.length;j++){
                            if(lineas[i].toLowerCase().contains(ingredientesValidos[j])){
                                variables.put(ingredientesValidos[j],Float.parseFloat(componenteslinea[0]));
                            }
                        }
                        if(lineas[i].contains("asada")){
                            ingredientesasados.add(lineas[i].trim());
                        }
                        textIngredientes.appendText(lineas[i].trim()+"\n");
                    }
                }
            }
        }
        System.out.println(variables.toString());
    }
    public String reemplazarCantidades(String ingredientes,float porcionesOriginal,float porcionesRequeridas){
        String ingsNew="";
        String[]lineas=ingredientes.split("\n");
        for(int i=0;i<lineas.length;i++) {
            for (int j = 0; j < ingredientesValidos.length; j++) {
                if (lineas[i].toLowerCase().contains(ingredientesValidos[j])){
                    if(Math.round(variables.get(ingredientesValidos[j]))==variables.get(ingredientesValidos[j])){
                        lineas[i]=lineas[i].replace(String.valueOf(Math.round(variables.get(ingredientesValidos[j]))),String.valueOf(((variables.get(ingredientesValidos[j]))*(porcionesRequeridas/porcionesOriginal))));

                    }
                    else{
                        lineas[i]=lineas[i].replace(String.valueOf(variables.get(ingredientesValidos[j])),String.valueOf(((variables.get(ingredientesValidos[j]))*(porcionesRequeridas/porcionesOriginal))));
                    }
                }
            }
            ingsNew+="\n"+lineas[i];
        }
        return ingsNew;
    }
}
