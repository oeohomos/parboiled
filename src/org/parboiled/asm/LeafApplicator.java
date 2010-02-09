/*
 * Copyright (C) 2009-2010 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.parboiled.asm;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.parboiled.common.Preconditions;

class LeafApplicator implements ClassTransformer, Opcodes, Types {

    private final ClassTransformer nextTransformer;

    public LeafApplicator(ClassTransformer nextTransformer) {
        this.nextTransformer = nextTransformer;
    }

    public ParserClassNode transform(@NotNull ParserClassNode classNode) throws Exception {
        for (ParserMethod method : classNode.leafMethods) {
            createLeafMarkingCode(method);
        }

        return nextTransformer != null ? nextTransformer.transform(classNode) : classNode;
    }

    @SuppressWarnings({"unchecked"})
    private void createLeafMarkingCode(ParserMethod method) {
        InsnList instructions = method.instructions;
        AbstractInsnNode current = instructions.getFirst();

        while (current.getOpcode() != ARETURN) {
            current = current.getNext();
        }

        // stack: <rule>
        instructions.insertBefore(current, new MethodInsnNode(INVOKEINTERFACE, RULE_TYPE.getInternalName(),
                "asLeaf", "()" + RULE_TYPE.getDescriptor()));
        // stack: <rule>
    }

    public String getLabelText(ParserMethod method) {
        if (method.visibleAnnotations != null) {
            for (Object annotationObj : method.visibleAnnotations) {
                AnnotationNode annotation = (AnnotationNode) annotationObj;
                if (annotation.desc.equals(LABEL_TYPE.getDescriptor()) && annotation.values != null) {
                    Preconditions.checkState("label".equals(annotation.values.get(0)));
                    return (String) annotation.values.get(1);
                }
            }
        }
        return method.name;
    }

}