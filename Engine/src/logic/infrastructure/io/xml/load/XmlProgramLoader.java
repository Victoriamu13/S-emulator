package logic.infrastructure.io.xml.load;

import logic.domain.program.functions.FunctionRepository;
import logic.infrastructure.io.xml.build.XmlProgramBuilder;
import logic.infrastructure.io.xml.parser.programParse.ProgramParseResult;
import logic.infrastructure.io.xml.parser.programParse.XmlProgramParser;
import logic.infrastructure.io.xml.parser.utils.BasicFileChecks;
import logic.infrastructure.io.xml.validation.ValidateResult;
import logic.infrastructure.io.xml.validation.XmlProgramValidator;
import logic.domain.program.SProgram;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static logic.infrastructure.io.xml.parser.utils.DomUtils.first;

public class XmlProgramLoader {
    private final XmlProgramParser parser = new XmlProgramParser();
    private final XmlProgramValidator validator = new XmlProgramValidator();
    private final XmlProgramBuilder builder = new XmlProgramBuilder();


    public LoadResult load(Path xmlPath) {

        //BasicFileChecks
        List<String> errors = new ArrayList<>(BasicFileChecks.validate(xmlPath));
         if (!errors.isEmpty()) {
            return LoadResult.failed(errors);
         }

         // 1) Parse
         ProgramParseResult parsed = parser.parse(xmlPath);
         if (!parsed.errors().isEmpty()) {
             return LoadResult.failed(parsed.errors());
         }

         // 2) Validate
         ValidateResult validated = validator.validate(parsed.programName(), parsed.raw(),parsed.functions());
         if (!validated.errors().isEmpty()) {
             return LoadResult.failed(validated.errors());
         }

        SProgram program = builder.build(parsed.programName(), parsed.raw());


        // 4) Build repository of functions
        FunctionRepository repo = new FunctionRepository();
        builder.registerFunctions(parsed.functions(), repo);
        program.setFunctionLookup(repo);

        return LoadResult.success(program, repo);
    }
}