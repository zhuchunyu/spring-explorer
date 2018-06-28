package com.rest.editor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.stream.XMLStreamReader;

@RestController
public class ModelEditorJsonRestResource
        implements ModelDataJsonConstants {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ModelEditorJsonRestResource.class);

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping(value = {"/service/model/{modelId}/json"}, method = {RequestMethod.GET}, produces = {"application/json"})
    public ObjectNode getEditorJson(@PathVariable String modelId) {
        ObjectNode modelNode = null;

        System.out.println("ModelEditorJsonRestResource.getEditorJson---------");
        Model model = this.repositoryService.getModel(modelId);

        if (model != null) {
            try {
                if (StringUtils.isNotEmpty(model.getMetaInfo())) {
                    modelNode = (ObjectNode) this.objectMapper.readTree(model.getMetaInfo());
                } else {
                    modelNode = this.objectMapper.createObjectNode();
                    modelNode.put("name", model.getName());
                }
                modelNode.put("modelId", model.getId());
                ObjectNode editorJsonNode = (ObjectNode) this.objectMapper.readTree(new String(this.repositoryService
                        .getModelEditorSource(model
                                .getId()), "utf-8"));


                String a  = new String(this.repositoryService.getModelEditorSource(model.getId()), "utf-8");
                System.out.println(a);

                // 生成bpmn文件
                JsonNode editorNode = new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelId));
                BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
                BpmnModel modelXml = jsonConverter.convertToBpmnModel(editorNode);
                byte[] bytes = new BpmnXMLConverter().convertToXML(modelXml);
                System.out.println(new String(bytes, "UTF-8"));
                BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(null);

                modelNode.put("model", editorJsonNode);
            } catch (Exception e) {
                LOGGER.error("Error creating model JSON", e);
                throw new ActivitiException("Error creating model JSON", e);
            }
        }
        return modelNode;
    }
}