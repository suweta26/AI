/*
 * Module which prints output.
 */
package iitg.cs561.project.alphabet.util;

import dnl.utils.text.table.TextTable;
import iitg.cs561.project.alphabet.DataFileProcessor;

/**
 *
 * @author Neelesh
 */
public class OutputPrinter {

    public void printResults(DataFileProcessor fp, CommonVarMethods cvm) {

        int i, j, k;
        int[][] freq = cvm.getFreq();
        Object[][] Result = new Object[AlphabetConstants.NUM_ACTIVITIES][AlphabetConstants.NUM_ACTIVITIES + 2];
        String[] ResultColumnLabels = new String[AlphabetConstants.NUM_ACTIVITIES + 2];
        int[] afreq = fp.getActivityCountInWhole();

        ResultColumnLabels[0] = "Actaul/Class Label";
        for (i = 0; i < AlphabetConstants.NUM_ACTIVITIES; i++) {
            ResultColumnLabels[i + 1] = AlphabetConstants.ACTIVITY_LABELS[i];
            Result[i][0] = AlphabetConstants.ACTIVITY_LABELS[i];
            for (j = 0; j < AlphabetConstants.NUM_ACTIVITIES; j++) {
                Result[i][j + 1] = freq[i][j];
            }
        }

        ResultColumnLabels[AlphabetConstants.NUM_ACTIVITIES + 1] = "Accuracy";
        System.out.print("\n\n");
        for (i = 0; i < AlphabetConstants.NUM_ACTIVITIES; i++) {

            if (freq[i][i] == 0) {
                Result[i][AlphabetConstants.NUM_ACTIVITIES + 1] = 0;
            } else {
                Result[i][AlphabetConstants.NUM_ACTIVITIES + 1] = (float) freq[i][i] / (float) afreq[i];
            }

        }
        TextTable tt = new TextTable(ResultColumnLabels, Result);
        tt.printTable();
        System.out.print("\n\n     ");                                                 // Print accuracy results
        System.out.print("Result: ");
        System.out.print("\n\n     ");
        System.out.print("Right " + cvm.getRight() + "\n     Wrong " + cvm.getWrong() + " \n     Average accuracy is " + ((float) cvm.getRight() / (float) (cvm.getRight() + cvm.getWrong())) + "\n");

    }

}
